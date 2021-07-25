package org.openntf.nsfodp.commons.h;

/**
 * Useful constants and derivatives from nsfnote.h.
 * 
 * @since 3.5.0
 */
public interface NsfNote {
	int NOTE_ID_SPECIAL            = 0xFFFF0000;
	short NOTE_CLASS_ACL             = 0x0040;
	short NOTE_CLASS_FORM            = 0x0004;
	short NOTE_CLASS_VIEW            = 0x0008;
	short NOTE_CLASS_HELP            = 0x0100;
	short NOTE_CLASS_ICON            = 0x0010;
	short NOTE_CLASS_INFO            = 0x0002;
	short NOTE_CLASS_FIELD           = 0x0400;
	short NOTE_CLASS_DESIGN          = 0x0020;
	short NOTE_CLASS_FILTER          = 0x0200;
	short NOTE_CLASS_REPLFORMULA     = 0x0800;
	short NOTE_CLASS_NONPRIV         = 0x0FFF;
	short NOTE_CLASS_DEFAULT         = (short)0x8000;
}
