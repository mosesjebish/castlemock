/*
 * Copyright 2015 Karl Dahlgren
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

package com.castlemock.web.basis.model.user.service;

import com.castlemock.core.basis.model.Repository;
import com.castlemock.core.basis.model.ServiceResult;
import com.castlemock.core.basis.model.ServiceTask;
import com.castlemock.core.basis.model.user.domain.Role;
import com.castlemock.core.basis.model.user.domain.Status;
import com.castlemock.core.basis.model.user.domain.User;
import com.castlemock.core.basis.model.user.dto.UserDto;
import com.castlemock.core.basis.model.user.service.message.input.ReadUsersByRoleInput;
import com.castlemock.core.basis.model.user.service.message.output.ReadUsersByRoleOutput;
import org.dozer.DozerBeanMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karl Dahlgren
 * @since 1.0
 */
public class ReadUsersByRoleServiceTest {

    @Spy
    private DozerBeanMapper mapper;

    @Mock
    private Repository repository;

    @InjectMocks
    private ReadUsersByRoleService service;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testProcess(){
        List<UserDto> users = new ArrayList<UserDto>();
        UserDto user = new UserDto();
        user.setId(new String());
        user.setUsername("Username");
        user.setStatus(Status.ACTIVE);
        user.setRole(Role.ADMIN);
        user.setEmail("email@email.com");
        users.add(user);


        Mockito.when(repository.findAll()).thenReturn(users);
        final ReadUsersByRoleInput input = new ReadUsersByRoleInput(Role.ADMIN);
        final ServiceTask<ReadUsersByRoleInput> serviceTask = new ServiceTask<ReadUsersByRoleInput>();
        serviceTask.setInput(input);
        final ServiceResult<ReadUsersByRoleOutput> serviceResult = service.process(serviceTask);
        final ReadUsersByRoleOutput output = serviceResult.getOutput();

        final List<UserDto> returnedUsers = output.getUsers();
        Assert.assertNotNull(returnedUsers);
        Assert.assertEquals(users.size(), returnedUsers.size());
        final UserDto returnedUser = returnedUsers.get(0);
        Assert.assertEquals(user.getId(), returnedUser.getId());
        Assert.assertEquals(user.getEmail(), returnedUser.getEmail());
        Assert.assertEquals(user.getRole(), returnedUser.getRole());
        Assert.assertEquals(user.getStatus(), returnedUser.getStatus());
        Assert.assertEquals(user.getUsername(), returnedUser.getUsername());
    }


}
