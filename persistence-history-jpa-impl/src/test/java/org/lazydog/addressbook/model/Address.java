/**
 * Copyright 2010-2013 lazydog.org.
 *
 * This file is part of persistence history.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.lazydog.addressbook.model;

/**
 * Address.
 *
 * @author  Ron Rickard
 */
public class Address {

    private String city;
    private Integer id;
    private String state;

    /**
     * Get the city.
     *
     * @return  the city.
     */
    public String getCity() {
        return this.city;
    }

    /**
     * Get the ID.
     *
     * @return  the ID.
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * Get the state.
     *
     * @return  the state.
     */
    public String getState() {
        return this.state;
    }

    /**
     * Set the city.
     *
     * @param  city  the city.
     */
    public void setCity(final String city) {
        this.city = city;
    }

    /**
     * Set the ID.
     *
     * @param  id  the ID.
     */
    public void setId(final Integer id) {
        this.id = id;
    }

    /**
     * Set the state.
     *
     * @param  state  the state.
     */
    public void setState(final String state) {
        this.state = state;
    }
}
