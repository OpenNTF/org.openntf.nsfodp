/**
 * Copyright © 2018-2022 Jesse Gallagher
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
package org.openntf.nsfodp.commons.dxl;

import org.openntf.nsfodp.commons.h.Ods;

public enum ODSConstants {
	;

	// *******************************************************************************
	// * CD record and item caps
	// *******************************************************************************
	
	// It appears that CDFILESEGMENTs cap out at 10240 bytes of data
	public static final int FILE_SEGMENT_SIZE_CAP = 10240;
	/** The amount of data to store in each CD record item */
	public static final int PER_FILE_ITEM_DATA_CAP = (Ods.SIZE_CDFILESEGMENT + FILE_SEGMENT_SIZE_CAP) * 2;
	
	public static final int IMAGE_SEGMENT_SIZE_CAP = 10250;
	/** The amount of data to store in each CD image record item */
	public static final int PER_IMAGE_ITEM_DATA_CAP = (Ods.SIZE_CDIMAGESEGMENT + IMAGE_SEGMENT_SIZE_CAP) * 2;
	
	public static final int BLOBPART_SIZE_CAP = 20000;
	/** The amount of data to store in each CD image record item */
	public static final int PER_BLOB_ITEM_DATA_CAP = (Ods.SIZE_CDBLOBPART + BLOBPART_SIZE_CAP) * 2;
}
