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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * A data class representing the Annotations of a Status or a Tweet
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
public class Annotations {

	public static final int lengthLimit = 512;
	private List<Annotation> annotations = null;

	/**
	 * Construct empty Annotations instance
	 */
	public Annotations() {
		setAnnotations(null);
	}

	/**
	 * Construct Annotations instance from a JSON Array, returned from Twitter
	 * API Package visibility only!
	 * 
	 * @param jsonArray - the JSON Array
	 */
	public Annotations(final JSONArray jsonArray) {
		setAnnotations(null);
		try {
			for (int i = 0; i < jsonArray.length(); i++) {
				addAnnotation(new Annotation(jsonArray.getJSONObject(i)));
			}
		} catch (final JSONException jsone) {
			// malformed JSONObject - just empty the list of Annotations
			annotations.clear();
		}
	}

	/**
	 * Construct Annotations instance from an exisiting List of Annotation
	 * instances
	 * 
	 * @param annotations - the List of Annotation instances
	 */
	public Annotations(final List<Annotation> annotations) {
		setAnnotations(annotations);
	}

	/**
	 * Adds a single Annotation to the List of Annotation instances
	 * 
	 * @param annotation - the Annotation to add
	 */
	public void addAnnotation(final Annotation annotation) {
		annotations.add(annotation);
	}

	/**
	 * Adds a single Annotation to the List of Annotation instances
	 * 
	 * @param annotation - the Annotation to add
	 * @return this (for coding convenience)
	 */
	public Annotations annotation(final Annotation annotation) {
		addAnnotation(annotation);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (null == obj) return false;
		if (this == obj) return true;
		// shouldn't use the List directly as the Annotations are equal
		// regardless of the order of the Annotation instances
		return obj instanceof Annotations && ((Annotations) obj).getSortedAnnotations().equals(getSortedAnnotations());
	}

	/**
	 * @return the List of Annotation instances
	 */
	public List<Annotation> getAnnotations() {
		return annotations;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getSortedAnnotations().hashCode();
	}

	/**
	 * @return true if the List of Annotation instances is empty, false
	 *         otherwise
	 */
	public boolean isEmpty() {
		return annotations.isEmpty();
	}

	/**
	 * @return true if the JSON String representation of this exceeds the limit
	 *         imposed by Twitter, false otherwise
	 */
	public boolean isExceedingLengthLimit() {
		return isExceedingLengthLimit(this);
	}

	/**
	 * Sets the List of Annotation instances Ensures the class's property is not
	 * null
	 * 
	 * @param annotations - the List of Annotation instances
	 */
	public void setAnnotations(final List<Annotation> annotations) {
		this.annotations = annotations == null ? new ArrayList<Annotation>() : annotations;
	}

	/**
	 * @return the number of Annotation instances in the List
	 */
	public Integer size() {
		return annotations.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("Annotations{");
		for (int i = 0; i < size(); i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(annotations.get(i).toString());
		}
		sb.append('}');

		return sb.toString();
	}

	/**
	 * @return a sorted copy of the List of Annotation instances
	 */
	private List<Annotation> getSortedAnnotations() {
		// create a new list - we do not want to change the order in the
		// original one
		final List<Annotation> sortedAnnotations = new ArrayList<Annotation>(size());
		sortedAnnotations.addAll(annotations);
		Collections.sort(sortedAnnotations);
		return sortedAnnotations;
	}

	/**
	 * Package visibility only!
	 * 
	 * @return the JSON String representation of this
	 */
	String asParameterValue() {
		final JSONArray jsonArray = new JSONArray();
		for (final Annotation annotation : annotations) {
			jsonArray.put(annotation.asJSONObject());
		}
		return jsonArray.toString();
	}

	/**
	 * @param annotations - the instance to test
	 * @return true if the JSON String representation of the instance exceeds
	 *         the limit imposed by Twitter, false otherwise
	 */
	public static boolean isExceedingLengthLimit(final Annotations annotations) {
		return annotations.asParameterValue().length() > lengthLimit;
	}

}
