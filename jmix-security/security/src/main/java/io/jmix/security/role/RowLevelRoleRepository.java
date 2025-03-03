/*
 * Copyright 2020 Haulmont.
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
 */

package io.jmix.security.role;

import io.jmix.security.model.RowLevelRole;

public interface RowLevelRoleRepository extends RoleRepository<RowLevelRole> {

    default RowLevelRole getRoleByCode(String code) {
        RowLevelRole rowLevelRole = findRoleByCode(code);
        if (rowLevelRole == null) {
            throw new IllegalStateException(String.format("RowLevelRole not found by code: %s", code));
        }
        return rowLevelRole;
    }
}
