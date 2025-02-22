/*
 * Copyright 2018 Karl Dahlgren
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.castlemock.web.mock.soap.controller.rest;

import com.castlemock.model.core.ServiceProcessor;
import com.castlemock.model.mock.soap.domain.SoapProject;
import com.castlemock.service.core.manager.FileManager;
import com.castlemock.service.mock.soap.project.input.CreateSoapPortsInput;
import com.castlemock.service.mock.soap.project.input.ReadSoapProjectInput;
import com.castlemock.service.mock.soap.project.input.UpdateSoapPortsForwardedEndpointInput;
import com.castlemock.service.mock.soap.project.input.UpdateSoapPortsStatusInput;
import com.castlemock.service.mock.soap.project.output.ReadSoapProjectOutput;
import com.castlemock.web.core.controller.rest.AbstractRestController;
import com.castlemock.web.mock.soap.model.LinkWsdlRequest;
import com.castlemock.web.mock.soap.model.UpdateSoapPortForwardedEndpointsRequest;
import com.castlemock.web.mock.soap.model.UpdateSoapPortStatusesRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("api/rest/soap")
@Api(value="SOAP - Project", description="REST Operations for Castle Mock SOAP Project", tags = {"SOAP - Project"})
public class SoapProjectRestController extends AbstractRestController {

    private final FileManager fileManager;

    @Autowired
    public SoapProjectRestController(final ServiceProcessor serviceProcessor,
                                     final FileManager fileManager){
        super(serviceProcessor);
        this.fileManager = Objects.requireNonNull(fileManager);
    }

    @ApiOperation(value = "Get Project", response = SoapProject.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved SOAP project")})
    @RequestMapping(method = RequestMethod.GET, value = "/project/{projectId}")
    @PreAuthorize("hasAuthority('READER') or hasAuthority('MODIFIER') or hasAuthority('ADMIN')")
    public @ResponseBody
    ResponseEntity<SoapProject> getProject(
            @ApiParam(name = "projectId", value = "The id of the project")
            @PathVariable(value = "projectId") final String projectId){
        final ReadSoapProjectOutput output = super.serviceProcessor.process(ReadSoapProjectInput.builder()
                .projectId(projectId)
                .build());
        return ResponseEntity.ok(output.getProject());
    }

    @ApiOperation(value = "Update Port statuses")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated SOAP port statuses")})
    @RequestMapping(method = RequestMethod.PUT, value = "/project/{projectId}/port/status")
    @PreAuthorize("hasAuthority('MODIFIER') or hasAuthority('ADMIN')")
    public @ResponseBody
    ResponseEntity<Void> updatePortStatuses(
            @ApiParam(name = "projectId", value = "The id of the project")
            @PathVariable(value = "projectId") final String projectId,
            @RequestBody UpdateSoapPortStatusesRequest request){
        request.getPortIds()
                .forEach(portId -> super.serviceProcessor.process(UpdateSoapPortsStatusInput.builder()
                        .projectId(projectId)
                        .portId(portId)
                        .operationStatus(request.getStatus())
                        .build()));
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Update Port forwarded endpoints")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated SOAP port forwarded endpoints")})
    @RequestMapping(method = RequestMethod.PUT, value = "/project/{projectId}/port/endpoint/forwarded")
    @PreAuthorize("hasAuthority('MODIFIER') or hasAuthority('ADMIN')")
    public @ResponseBody
    ResponseEntity<Void> updatePortForwardedEndpoints(
            @ApiParam(name = "projectId", value = "The id of the project")
            @PathVariable(value = "projectId") final String projectId,
            @RequestBody UpdateSoapPortForwardedEndpointsRequest request){
        super.serviceProcessor.process(UpdateSoapPortsForwardedEndpointInput.builder()
                .projectId(projectId)
                .portIds(request.getPortIds())
                .forwardedEndpoint(request.getForwardedEndpoint())
                .build());
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Upload WSDL")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully uploaded WSDL")})
    @RequestMapping(method = RequestMethod.POST, value = "/project/{projectId}/wsdl/file")
    @PreAuthorize("hasAuthority('MODIFIER') or hasAuthority('ADMIN')")
    public @ResponseBody
    ResponseEntity<Void> uploadWSDL(
            @ApiParam(name = "projectId", value = "The id of the project")
            @PathVariable(value = "projectId") final String projectId,
            @RequestParam("file") final MultipartFile multipartFile,
            @RequestParam("generateResponse") final boolean generateResponse){

        try {
            final File file = fileManager.uploadFile(multipartFile);
            super.serviceProcessor.process(CreateSoapPortsInput.builder()
                    .projectId(projectId)
                    .files(List.of(file))
                    .generateResponse(generateResponse)
                    .includeImports(false)
                    .build());
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @ApiOperation(value = "Link WSDL")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully linked WSDL")})
    @RequestMapping(method = RequestMethod.POST, value = "/project/{projectId}/wsdl/link")
    @PreAuthorize("hasAuthority('MODIFIER') or hasAuthority('ADMIN')")
    public @ResponseBody
    ResponseEntity<Void> linkWSDL(
            @ApiParam(name = "projectId", value = "The id of the project")
            @PathVariable(value = "projectId") final String projectId,
            @RequestBody final LinkWsdlRequest request){
        super.serviceProcessor.process(CreateSoapPortsInput.builder()
                .projectId(projectId)
                .files(null)
                .location(request.getUrl())
                .includeImports(request.getIncludeImports())
                .generateResponse(request.getGenerateResponse())
                .build());
        return ResponseEntity.ok().build();
    }

}
