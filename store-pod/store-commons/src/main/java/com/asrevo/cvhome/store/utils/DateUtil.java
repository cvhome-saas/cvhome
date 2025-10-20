/*
 * Licensed to csti consulting
 * You may obtain a copy of the License at
 *
 * http://www.csticonsulting.com
 * Copyright (c) 2006-Aug 24, 2010 Consultation CS-TI inc.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.asrevo.cvhome.store.utils;

import com.asrevo.cvhome.store.core.constants.Constants;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class DateUtil {

	private static final String LONGDATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss Z";

	/**
	 * yyyy-MM-dd
	 */
	public static String formatDate(Date dt) {

		if (dt == null)
			dt = new Date();
		SimpleDateFormat format = new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT);
		return format.format(dt);
	}

	public static String formatLongDate(Date date) {

		if (date == null)
			return null;
		SimpleDateFormat format = new SimpleDateFormat(LONGDATE_FORMAT);
		return format.format(date);
	}

	public static Date getDate(String date) throws Exception {
		DateFormat myDateFormat = new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT);
		return myDateFormat.parse(date);
	}

	public static String getPresentDate() {

		Date dt = new Date();

		SimpleDateFormat format = new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT);
		return format.format(new Date(dt.getTime()));
	}

	public static boolean dateBeforeEqualsDate(Date firstDate, Date compareDate) {

		if (firstDate == null || compareDate == null) {
			return true;
		}

		if (firstDate.compareTo(compareDate) > 0) {
			return false;
		}
		else if (firstDate.compareTo(compareDate) < 0) {
			return true;
		}
		else
			return firstDate.compareTo(compareDate) == 0;
	}

}
