/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package twitter4j;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A data class representing an Annotation of a Status or a Tweet
 * 
 * @author Roy Reshef - royreshef at gmail.com
 * @see <a href="http://dev.twitter.com/pages/annotations_overview">Annotations
 *      Overview | Twitter Developers</a>
 * @since Twitter4J 2.1.4
 * @deprecated Annotations is not available for now. <a href=
 *             "http://groups.google.com/group/twitter-development-talk/browse_thread/thread/4d5ff2ec4d2ce4a7"
 *             >Annotations - Twitter Development Talk | Google Groups</a>
 */
@Deprecated
public class Annotation implements Comparable<Annotation>, java.io.Serializable {
	private static final long serialVersionUID = -6515375141284988754L;
	private String type = null;
	private Map<String, String> attributes = null;

	/**
	 * Construct an Annotation with a type but no attributes
	 * 
	 * @param type - the type
	 */
	public Annotation(String type) {
		setType(type);
		setAttributes(null);
	}

	/**
	 * Construct an Annotation with a type and attributes
	 * 
	 * @param type
	 * @param attributes
	 */
	public Annotation(String type, Map<String, String> attributes) {
		setType(type);
		setAttributes(attributes);
	}

	/**
	 * Construct an Annotation instance from a JSON Object, returned from
	 * Twitter API Package visibility only!
	 * 
	 * @param jsonObject - the JSON Object
	 */
	Annotation(JSONObject jsonObject) {
		String typ = null;
		Map<String, String> attrs = null;
		@SuppressWarnings("rawtypes")
		Iterator it = jsonObject.keys();
		if (it.hasNext()) {
			typ = (String) it.next();
			// we expect only one key - the type; if there are more it's a
			// malformed JSON object
			if (it.hasNext()) {
				type = null;
			} else {
				try {
					final JSONObject jo = jsonObject.getJSONObject(typ);
					attrs = new LinkedHashMap<String, String>();
					it = jo.keys();
					while (it.hasNext()) {
						final String key = (String) it.next();
						final String value = jo.getString(key);
						attrs.put(key, value);
					}
				} catch (final JSONException jsone) {
					// clear all
					typ = null;
					attrs = null;
				}
			}
		}
		setType(typ);
		setAttributes(attrs);
	}

	/**
	 * Adds a single attribute
	 * 
	 * @param name - the attribute name
	 * @param value - the attribute value
	 */
	public void addAttribute(String name, String value) {
		attributes.put(name, value);
	}

	/**
	 * Adds a single attribute
	 * 
	 * @param name - the attribute name
	 * @param value - the attribute value
	 * @return this (for coding convenience)
	 */
	public Annotation attribute(String name, String value) {
		addAttribute(name, value);
		return this;
	}

	/**
	 * Sets the attributes Ensures the class's property is not null
	 * 
	 * @param attributes - the attributes
	 * @return this (for coding convenience)
	 */
	public Annotation attributes(Map<String, String> attributes) {
		setAttributes(attributes);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Annotation other) {
		// Precedence of order:
		// 1. Type
		// 2. Number of attributes
		// 3. Sorted attributes - For each attribute: name, then value
		if (null == other) return 1;
		if (this == other) return 0;
		int result = getType().compareTo(other.getType());
		if (result != 0) return result;
		result = size().compareTo(other.size());
		if (result != 0) return result;

		final Iterator<String> thisNamesIt = sortedNames().iterator();
		final Iterator<String> otherNamesIt = other.sortedNames().iterator();

		while (thisNamesIt.hasNext()) {
			final String thisName = thisNamesIt.next();
			final String otherName = otherNamesIt.next();
			result = thisName.compareTo(otherName);
			if (result != 0) return result;
			final String thisValue = getAttributes().get(thisName);
			final String otherValue = other.getAttributes().get(otherName);
			result = thisValue.compareTo(otherValue);
			if (result != 0) return result;
		}
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (null == obj) return false;
		if (this == obj) return true;
		if (!(obj instanceof Annotation)) return false;
		final Annotation other = (Annotation) obj;
		// Map comparison ignores the order of the map entries -
		// which is exactly what we want here for the attributes
		return getType().equals(other.getType()) && getAttributes().equals(other.getAttributes());
	}

	/**
	 * @return the attributes
	 */
	public Map<String, String> getAttributes() {
		return attributes;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return 31 * type.hashCode() + attributes.hashCode();
	}

	/**
	 * @return true if the attributes are empty, false otherwise
	 */
	public boolean isEmpty() {
		return attributes.isEmpty();
	}

	/**
	 * Sets the attributes Ensures the class's property is not null
	 * 
	 * @param attributes - the attributes
	 */
	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes == null ? new LinkedHashMap<String, String>() : attributes;
	}

	/**
	 * Sets the type Ensures the class's property is not null
	 * 
	 * @param type - the type
	 */
	public void setType(String type) {
		this.type = type == null ? "" : type;
	}

	/**
	 * @return true the number of attributes
	 */
	public Integer size() {
		return attributes.size();
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("Annotation{type='");
		sb.append(type).append("', attributes={");
		final Iterator<String> nameIt = attributes.keySet().iterator();
		while (nameIt.hasNext()) {
			final String name = nameIt.next();
			final String value = attributes.get(name);
			sb.append('\'').append(name).append("'='").append(value).append('\'');
			if (nameIt.hasNext()) {
				sb.append(", ");
			}
		}
		sb.append("}}");
		return sb.toString();
	}

	/**
	 * Sets the type Ensures the class's property is not null
	 * 
	 * @param type - the type
	 * @return this (for coding convenience)
	 */
	public Annotation type(String type) {
		setType(type);
		return this;
	}

	/**
	 * @return a sorted set of the attributes' names
	 */
	private SortedSet<String> sortedNames() {
		final SortedSet<String> names = new TreeSet<String>();
		names.addAll(getAttributes().keySet());
		return names;
	}

	/**
	 * Package visibility only! Converts this to a JSON object according to
	 * Twitter's specification
	 * 
	 * @return the JSON Object
	 */
	JSONObject asJSONObject() {
		final JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(type, attributes);
		} catch (final JSONException ignore) {
		}
		return jsonObject;
	}

	/**
	 * Package visibility only!
	 * 
	 * @return the JSON String representation of this
	 */
	String asParameterValue() {
		return asJSONObject().toString();
	}

}
