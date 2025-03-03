/*
 * Copyright (c) 2008-2020 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.jmix.rest.impl.service.filter.data;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.annotation.Nullable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PermissionsInfo {
    private List<String> authorities;
    private List<ShortPermissionInfo> entities;
    private List<ShortPermissionInfo> entityAttributes;
    private List<ShortPermissionInfo> specifics;

    public PermissionsInfo() {
    }

    @Nullable
    public List<String> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(List<String> authorities) {
        this.authorities = authorities;
    }

    @Nullable
    public List<ShortPermissionInfo> getEntities() {
        return entities;
    }

    public void setEntities(List<ShortPermissionInfo> entities) {
        this.entities = entities;
    }

    @Nullable
    public List<ShortPermissionInfo> getEntityAttributes() {
        return entityAttributes;
    }

    public void setEntityAttributes(List<ShortPermissionInfo> entityAttributes) {
        this.entityAttributes = entityAttributes;
    }

    @Nullable
    public List<ShortPermissionInfo> getSpecifics() {
        return specifics;
    }

    public void setSpecifics(List<ShortPermissionInfo> specifics) {
        this.specifics = specifics;
    }
}
