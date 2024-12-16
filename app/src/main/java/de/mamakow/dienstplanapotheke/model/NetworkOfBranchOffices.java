/*
 * Copyright (C) 2021 Mandelkow
 *
 * Dienstplan Apotheke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package de.mamakow.dienstplanapotheke.model;

import java.util.Map;

public class NetworkOfBranchOffices {
    private Map<Integer, Branch> listOfBranches;

    public NetworkOfBranchOffices() {
        /*
         * @TODO: 15.12.2024 We need the data from the API.
         */
        //listOfBranches = readFromFile();
    }

    public Map<Integer, Branch> getListOfBranches() {
        return listOfBranches;
    }

    public Branch getBranchById(int branchId) {
        if (0 == branchId && !listOfBranches.containsKey(0)) {
            return getEmptyBranch();
        }
        return listOfBranches.get(branchId);
    }

    private Branch getEmptyBranch() {
        return new Branch(0, 0, "", "", "", "", null);
    }

    public Branch getBranchByName(String name) {
        Branch branchObject = null;
        for (Branch branch : listOfBranches.values()) {
            branchObject = branch;
            if (branchObject.getBranchName().equals(name)) {
                return branchObject;
            }
            if (branchObject.getBranchShortName().equals(name)) {
                return branchObject;
            }
        }
        if (name.isEmpty() && !listOfBranches.containsKey(0)) {
            return getEmptyBranch();
        }

        return branchObject;
    }
}
