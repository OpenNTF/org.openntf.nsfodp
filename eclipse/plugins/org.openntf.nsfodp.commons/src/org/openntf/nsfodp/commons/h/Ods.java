package org.openntf.nsfodp.commons.h;

/**
 * Useful constants and derivatives from ods.h.
 * 
 * @since 1.4.0
 */
public interface Ods {
	// *******************************************************************************
	// * General sizes
	// *******************************************************************************
	
	public static final int SIZE_BYTE = 1;
	public static final int SIZE_WORD = 2;
	public static final int SIZE_DWORD = 4;
	
	// *******************************************************************************
	// * CD structures
	// *******************************************************************************
	
	public static final int WORDRECORDLENGTH = 0xFF00;
	public static final int SIZE_LSIG = SIZE_WORD             // Signature
	                             + SIZE_DWORD;                // Length
	public static final int SIZE_WSIG = SIZE_WORD             // Signature
			                     + SIZE_WORD;                 // Length
	
	public static final short SIG_CDFILEHEADER = 97;
	public static final int SIZE_CDFILEHEADER = SIZE_LSIG     // Header
	                             + SIZE_WORD                  // FilleExtLen
	                             + SIZE_DWORD                 // FileDataSize
	                             + SIZE_DWORD                 // SegCount
	                             + SIZE_DWORD                 // Flags
	                             + SIZE_DWORD;                // Reserved
	public static final short SIG_CDFILESEGMENT = 96;
	public static final int SIZE_CDFILESEGMENT = SIZE_LSIG    // Header
			                     + SIZE_WORD                  // DataSize
	                             + SIZE_WORD                  // SegSize
	                             + SIZE_DWORD                 // Flags
	                             + SIZE_DWORD;                // Reserved
	public static final short SIG_CDIMAGEHEADER = 125;
	public static final int SIZE_CDIMAGEHEADER = SIZE_LSIG    // Signature
                                 + SIZE_WORD                  // ImageType
                                 + SIZE_WORD                  // Width
                                 + SIZE_WORD                  // Height
                                 + SIZE_DWORD                 // ImageDataSize
                                 + SIZE_DWORD                 // SegCount
                                 + SIZE_DWORD                 // Flags
                                 + SIZE_DWORD;                // Reserved
	public static final short SIG_CDIMAGESEGMENT = 124;
	public static final int SIZE_CDIMAGESEGMENT = SIZE_LSIG   // Header
                                 + SIZE_WORD                  // DataSize
                                 + SIZE_WORD;                 // SegSize
	public static final int SIZE_RECTSIZE = SIZE_WORD         // width
			                     + SIZE_WORD;                 // height
	public static final int SIZE_CROPRECT = SIZE_WORD         // left
			                     + SIZE_WORD                  // top
			                     + SIZE_WORD                  // right
			                     + SIZE_WORD;                 // bottom
	public static final short SIG_CDGRAPHIC = 153;
	public static final int SIZE_CDGRAPHIC = SIZE_LSIG        // Signature
			                     + SIZE_RECTSIZE              // DestSize
			                     + SIZE_RECTSIZE              // CropSize
			                     + SIZE_CROPRECT              // CropOffset
			                     + SIZE_WORD                  // fResize
			                     + SIZE_BYTE                  // Version
			                     + SIZE_BYTE                  // bFlags
			                     + SIZE_WORD;                 // wReserved
	public static final byte CDGRAPHIC_VERSION3 = 2;

	public static final short SIG_CDEVENT = (short)(249 | 0xFF00);
	public static final short SIZE_CDEVENT = SIZE_WSIG        // Signature
			                     + SIZE_DWORD                 // Flags
			                     + SIZE_WORD                  // EventType
			                     + SIZE_WORD                  // ActionType
			                     + SIZE_DWORD                 // ActionLength
			                     + SIZE_WORD                  // SignatureLength
			                     + SIZE_BYTE * 14;            // Reserved[14]
	public static final short SIG_CDBLOBPART = (short)(220 | WORDRECORDLENGTH);
	public static final int SIZE_CDBLOBPART = SIZE_WSIG       // Signature
			                     + SIZE_WORD                  // OwnerSig
			                     + SIZE_WORD                  // Length
			                     + SIZE_WORD                  // BlobMax
			                     + SIZE_BYTE * 8;             // Reserved[8]
	
	public static final short HTML_EVENT_LIBRARY = 22;
	public static final short ACTION_TYPE_JAVASCRIPT = 4;
}
