package org.openntf.nsfodp.commons.h;

/**
 * Useful constants from stdnames.h.
 * 
 * @since 1.4.0
 */
public interface StdNames {
	public static final String DESIGN_FLAGS = "$Flags"; //$NON-NLS-1$
	public static final String DESIGN_FLAGS_EXTENDED = "$FlagsExt"; //$NON-NLS-1$
	public static final String FIELD_TITLE = "$TITLE"; //$NON-NLS-1$
	public static final String IMAGE_NEW_DBICON_NAME = "IMAGE_NEW_DBICON_NAME"; //$NON-NLS-1$
	
	/**	Type of assistant - related to action type */
	public static final String ASSIST_TYPE_ITEM = "$AssistType"; //$NON-NLS-1$
	
	/** 	FORM: Indicates that a subform is in the add subform list  */
	public static final char DESIGN_FLAG_ADD = 'A';
	/** 	VIEW: Indicates that a view is an antifolder view  */
	public static final char DESIGN_FLAG_ANTIFOLDER = 'a';
	/** 	FILTER: Indicates FILTER_TYPE_BACKGROUND is asserted  */
	public static final char DESIGN_FLAG_BACKGROUND_FILTER = 'B';
	/** 	VIEW: Indicates view can be initially built only by designer and above  */
	public static final char DESIGN_FLAG_INITBYDESIGNONLY = 'b';
	/** 	FORM: Indicates a form that is used only for query by form (not on compose menu). */
	public static final char DESIGN_FLAG_NO_COMPOSE = 'C';
	/** 	VIEW: Indicates a form is a calendar style view.  */
	public static final char DESIGN_FLAG_CALENDAR_VIEW = 'c';
	/**  	FORM: Indicates a form that should not be used in query by form  */
	public static final char DESIGN_FLAG_NO_QUERY = 'D';
	/**  	ALL: Indicates the default design note for it's class (used for VIEW)  */
	public static final char DESIGN_FLAG_DEFAULT_DESIGN = 'd';
	/** 	FILTER: Indicates FILTER_TYPE_MAIL is asserted  */
	public static final char DESIGN_FLAG_MAIL_FILTER = 'E';
	/** 	VIEW: Indicates that a view is a public antifolder view  */
	public static final char DESIGN_FLAG_PUBLICANTIFOLDER = 'e';
	/** 	VIEW: This is a V4 folder view.  */
	public static final char DESIGN_FLAG_FOLDER_VIEW = 'F';
	/** 	FILTER: This is a V4 agent  */
	public static final char DESIGN_FLAG_V4AGENT = 'f';
	/** 	VIEW: This is ViewMap/GraphicView/Navigator  */
	public static final char DESIGN_FLAG_VIEWMAP = 'G';
	/**   FORM: file design element  */
	public static final char DESIGN_FLAG_FILE = 'g';
	/** 	ALL: Indicates a form that is placed in Other... dialog  */
	public static final char DESIGN_FLAG_OTHER_DLG = 'H';
	/**   Javascript library.  */
	public static final char DESIGN_FLAG_JAVASCRIPT_LIBRARY = 'h';
	/** 	FILTER: This is a V4 paste agent  */
	public static final char DESIGN_FLAG_V4PASTE_AGENT = 'I';
	/** 	FORM: Note is a shared image resource  */
	public static final char DESIGN_FLAG_IMAGE_RESOURCE = 'i';
	/**   FILTER: If its Java  */
	public static final char DESIGN_FLAG_JAVA_AGENT = 'J';
	/**  FILTER: If it is a java agent with java source code.  */
	public static final char DESIGN_FLAG_JAVA_AGENT_WITH_SOURCE = 'j';
	/**  to keep mobile digests out of form lists  */
	public static final char DESIGN_FLAG_MOBILE_DIGEST = 'K';
	/** 	FORM: with "g", design element is an xpage, much like a file resource, but special!  */
	public static final char DESIGN_FLAG_XSPPAGE = 'K';
	/**  Data Connection Resource (DCR) for 3rd party database  */
	public static final char DESIGN_FLAG_CONNECTION_RESOURCE = 'k';
	/**   FILTER: If its LOTUSSCRIPT  */
	public static final char DESIGN_FLAG_LOTUSSCRIPT_AGENT = 'L';
	/**   VIEW: Indicates that a view is a deleted documents view  */
	public static final char DESIGN_FLAG_DELETED_DOCS = 'l';
	/** 	FILTER: Stored FT query AND macro  */
	public static final char DESIGN_FLAG_QUERY_MACRO_FILTER = 'M';
	/**   FILTER: This is a site(m)ap.  */
	public static final char DESIGN_FLAG_SITEMAP = 'm';
	/**   FORM: Indicates that a subform is listed when making a new form. */
	public static final char DESIGN_FLAG_NEW = 'N';
	/**  ALL: notes stamped with this flag 
	will be hidden from Notes clients 
	We need a separate value here 
	because it is possible to be
	hidden from V4 AND to be hidden
	from Notes, and clearing one 
	should not clear the other */
	public static final char DESIGN_FLAG_HIDE_FROM_NOTES = 'n'; 
	/** 	FILTER: Indicates V4 search bar query object - used in addition to 'Q'  */
	public static final char DESIGN_FLAG_QUERY_V4_OBJECT = 'O';
	/**   VIEW: If Private_1stUse, store the private view in desktop  */
	public static final char DESIGN_FLAG_PRIVATE_STOREDESK = 'o';
	/** 	ALL: related to data dictionary  */
	public static final char DESIGN_FLAG_PRESERVE = 'P';
	/**  	VIEW: This is a private copy of a private on first use view.  */
	public static final char DESIGN_FLAG_PRIVATE_1STUSE = 'p';
	/** 	FILTER: Indicates full text query ONLY, no filter macro  */
	public static final char DESIGN_FLAG_QUERY_FILTER = 'Q';
	/** 	FILTER: Search part of this agent should be shown in search bar  */
	public static final char DESIGN_FLAG_AGENT_SHOWINSEARCH = 'q';
	/**	SPECIAL: this flag is the opposite of DESIGN_FLAG_PRESERVE, used
	only for the 'About' and 'Using' notes + the icon bitmap in the icon note */
	public static final char DESIGN_FLAG_REPLACE_SPECIAL = 'R';	
	/**   DESIGN: this flag is used to propagate the prohibition of design change  */
	public static final char DESIGN_FLAG_PROPAGATE_NOCHANGE = 'r';
	/** 	FILTER: This is a V4 background agent  */
	public static final char DESIGN_FLAG_V4BACKGROUND_MACRO = 'S';
	/** 	FILTER: A database global script library note  */
	public static final char DESIGN_FLAG_SCRIPTLIB = 's';
	/**  	VIEW: Indicates a view that is categorized on the categories field  */
	public static final char DESIGN_FLAG_VIEW_CATEGORIZED = 'T';
	/** 	FILTER: A database script note  */
	public static final char DESIGN_FLAG_DATABASESCRIPT = 't';
	/** 	FORM: Indicates that a form is a subform. */
	public static final char DESIGN_FLAG_SUBFORM = 'U';
	/** 	FILTER: Indicates agent should run as effective user on web  */
	public static final char DESIGN_FLAG_AGENT_RUNASWEBUSER = 'u';
	/**	FILTER: Indicates agent should run as invoker (generalize the
	web user notion, reuse the flag */
	public static final char DESIGN_FLAG_AGENT_RUNASINVOKER = 'u';	
	/**  	ALL: This is a private element stored in the database  */
	public static final char DESIGN_FLAG_PRIVATE_IN_DB = 'V';
	/**	FORM: Used with 'i' to indicate the image is an image well. 
	Used for images with images across, not images down.
	'v' looks like a bucket */
	public static final char DESIGN_FLAG_IMAGE_WELL = 'v';	
	/** 	FORM: Note is a WEBPAGE	 */
	public static final char DESIGN_FLAG_WEBPAGE = 'W';
	/**
	 * ALL: notes stamped with this flag will be hidden from WEB clients.
	 * 
	 * <p> WARNING: A formula that build Design Collecion relies on the fact that Agent Data's
	 * $Flags is the only Desing Collection element whose $Flags="X"</p>
	 */
	public static final char DESIGN_FLAG_HIDE_FROM_WEB = 'w'; 
	/**   FILTER: This is a V4 agent data note  */
	public static final char DESIGN_FLAG_V4AGENT_DATA = 'X';
	/**	SUBFORM: indicates whether
	we should render a subform in
	the parent form					*/
	public static final char DESIGN_FLAG_SUBFORM_NORENDER = 'x';
	/** 	ALL: Indicates that folder/view/etc. should be hidden from menu.  */
	public static final char DESIGN_FLAG_NO_MENU = 'Y';
	/** 	Shared actions note	 */
	public static final char DESIGN_FLAG_SACTIONS = 'y';
	/**  ALL: Used to indicate design element was hidden before the 'Notes Global Designer' modified it. (used with the "!" flag) */
	public static final char DESIGN_FLAG_MULTILINGUAL_PRESERVE_HIDDEN = 'Z';
	/**   FILTER: this is a servlet, not an agent!  */
	public static final char DESIGN_FLAG_SERVLET = 'z';
	/**   FORM: reuse obsoleted servlet flag  */
	public static final char DESIGN_FLAG_ACCESSVIEW = 'z';

	/** 	FORM: Indicates that this is a frameset note  */
	public static final char DESIGN_FLAG_FRAMESET = '#'; 
	/**
	 * ALL: Indicates this design element supports the 'Notes Global Designer' multilingual addin
	 */
	public static final char DESIGN_FLAG_MULTILINGUAL_ELEMENT = '!';
	/** 	FORM: Note is a shared Java resource  */
	public static final char DESIGN_FLAG_JAVA_RESOURCE = '@';
	/**  Style Sheet Resource (SSR)  */
	public static final char DESIGN_FLAG_STYLESHEET_RESOURCE = '=';
	/**  FILTER: web service design element  */
	public static final char DESIGN_FLAG_WEBSERVICE = '{';
	/**  VIEW: shared column design element  */
	public static final char DESIGN_FLAG_SHARED_COL = '^';

	/** 	hide this element from mobile clients  */
	public static final char DESIGN_FLAG_HIDE_FROM_MOBILE = '1';
	/** 	hide from portal  */
	public static final char DESIGN_FLAG_HIDE_FROM_PORTAL = '2';

	/**  xpage/cc properties file  */
	public static final char DESIGN_FLAG_PROPFILE = '2';

	/**
	 * ALL: notes stamped with this flag will be hidden from V3 client
	 */
	public static final char DESIGN_FLAG_HIDE_FROM_V3 = '3';
	/**	ALL: notes stamped with this flag 
	will be hidden from V4 client */
	public static final char DESIGN_FLAG_HIDE_FROM_V4 = '4';
	/**
	 * FILTER: 'Q5'= hide from V4.5 search list
	 * 
	 * <p>ALL OTHER: notes stamped with this flag will be hidden from V5 client</p>
	 */
	public static final char DESIGN_FLAG_HIDE_FROM_V5 = '5';
	/**	ALL: notes stamped with this flag 
	will be hidden from V6 client */
	public static final char DESIGN_FLAG_HIDE_FROM_V6 = '6';
	/**	ALL: notes stamped with this flag 
	will be hidden from V7 client */
	public static final char DESIGN_FLAG_HIDE_FROM_V7 = '7';
	/**	ALL: notes stamped with this flag 
	will be hidden from V8 client */
	public static final char DESIGN_FLAG_HIDE_FROM_V8 = '8';
	/**	ALL: notes stamped with this flag 
	will be hidden from V9 client */
	public static final char DESIGN_FLAG_HIDE_FROM_V9 = '9';
	/**	ALL: notes stamped with this flag 
	will be hidden from the client 
	usage is for different language
	versions of the design list to be
	hidden completely				*/
	public static final char DESIGN_FLAG_MUTILINGUAL_HIDE = '0';
	/** 	shimmer design docs  */
	public static final char DESIGN_FLAG_WEBHYBRIDDB = '%';

	/**   for files, at least for starters  */
	public static final char DESIGN_FLAG_READONLY = '&';
	/**  	for files, at least for now  */
	public static final char DESIGN_FLAG_NEEDSREFRESH = '$';
	/** 	this design element is an html file  */
	public static final char DESIGN_FLAG_HTMLFILE = '>';
	/**  	this design element is a jsp  */
	public static final char DESIGN_FLAG_JSP = '<';
	/**  VIEW - Query View in design list  */
	public static final char DESIGN_FLAG_QUERYVIEW = '<';
	/** 	this file element is a directory  */
	public static final char DESIGN_FLAG_DIRECTORY = '/';

	/** 	FORM - used for printing.  */
	public static final char DESIGN_FLAG_PRINTFORM = '?';
	/** 	keep this thing out of a design list  */
	public static final char DESIGN_FLAG_HIDEFROMDESIGNLIST = '~';
	/** 	keep this thing out of a design list but allow users to view doc using it  */
	public static final char DESIGN_FLAG_HIDEONLYFROMDESIGNLIST = '}';
	/**   FORM: This is a "composite application" design element. LI 3925.04  */
	public static final char DESIGN_FLAG_COMPOSITE_APP = '|';
	/**   FORM: Design element is "wiring properties". Always accompanied by hide flags for versions prior to 8.0. LI 3925.05  */
	public static final char DESIGN_FLAG_COMPOSITE_DEF = ':';
	/**  note class form, a custom control  */
	public static final char DESIGN_FLAG_XSP_CC = ';';
	/**  note class filter, with 's', server side JS script library  */
	public static final char DESIGN_FLAG_JS_SERVER = '.';
	/**  style kit design element  */
	public static final char DESIGN_FLAG_STYLEKIT = '`';
	/**  also has a g and is a file, but a component/widget design element  */
	public static final char DESIGN_FLAG_WIDGET = '_';
	/**  Java design element  */
	public static final char DESIGN_FLAG_JAVAFILE = '[';
	
	public static final char DESIGN_FLAG_JARFILE = ','; // Not actually documented
	
	/** Scriptlib is a web service consumer lib*/
	public static final char DESIGN_FLAGEXT_WEBSERVICELIB = 'W';
	/** for files, indicates it's in the web content directory */
	public static final char DESIGN_FLAGEXT_WEBCONTENTFILE = 'w';
	
	/**  display things editable at V4 search bar; version filtering  */
	public static final String DFLAGPAT_V4SEARCHBAR = "(+Qq-Bst5nmz*"; //$NON-NLS-1$
	/**  display things editable at search bar; version filtering  */
	public static final String DFLAGPAT_SEARCHBAR = "(+QM-st5nmz*"; //$NON-NLS-1$

	/* NOTE: DFLAGPAT_VIEWFORM... are deprecated and should not be used. These were previously used to filter three different
		note classes: form, view and field. But we're running out of unique flags, and would like to use appropriate
		flag patterns for each design class. */
	/**  display things editable with dialog box; version filtering  */
	public static final String DFLAGPAT_VIEWFORM = "-FQMUGXWy#i:|@0nK;g~%z^"; //$NON-NLS-1$
	/**  display things showable on the menu  */
	public static final String DFLAGPAT_VIEWFORM_MENUABLE = "-FQMUGXWy#i:|@40nK;g~%z^}"; //$NON-NLS-1$
	/**  display things editable with dialog box; no version filtering (for design)  */
	public static final String DFLAGPAT_VIEWFORM_ALL_VERSIONS = "-FQMUGXWy#i:|@K;g~%z^}"; //$NON-NLS-1$

	/**  display things editable with dialog box; version filtering  */
	public static final String DFLAGPAT_FORM = "-FQMUGXWy#i:|@0nK;g~%z^"; //$NON-NLS-1$
	/**  Form, page or subform design element, visible to client and web  */
	public static final String DFLAGPAT_FORM_OR_SIMILAR = "-FMGXy#i=:|@0K;g~%z^"; //$NON-NLS-1$
	/**  Form or page design element  */
	public static final String DFLAGPAT_FORM_OR_PAGE = "-FQMUGXy#i:|@0nK;g~%z^"; //$NON-NLS-1$
	/**  display things showable on the menu  */
	public static final String DFLAGPAT_FORM_MENUABLE = "-FQMUGXWy#i:|@40nK;g~%z^}"; //$NON-NLS-1$
	/**  display things editable with dialog box; no version filtering (for design)  */
	public static final String DFLAGPAT_FORM_ALL_VERSIONS = "-FQMUGXWy#i:|@K;g~%z^}"; //$NON-NLS-1$
	/**  display things editable with dialog box; no version filtering (for design)  */
	public static final String DFLAGPAT_PRINTFORM_ALL_VERSIONS = "+?"; //$NON-NLS-1$
	/**  display things editable with dialog box; version filtering (more complex than necessary,
														but trying to avoid any possible incompatibility vs. earlier use of DFLAGPAT_VIEWFORM)  */
	public static final String DFLAGPAT_FIELD = "-FQMUGXWy#i@0nK;g~%z^"; //$NON-NLS-1$
	/**  display things editable with dialog box; no version filtering (for design)  */
	public static final String DFLAGPAT_FIELD_ALL_VERSIONS = "-FQMUGXWy#i@K;g~%z^}"; //$NON-NLS-1$

	/**  display things that are runnable; version filtering  */
	public static final String DFLAGPAT_TOOLSRUNMACRO = "-QXMBESIst5nmz{"; //$NON-NLS-1$
	/**  display things that show up in agents list. No version filtering (for design)  */
	public static final String DFLAGPAT_AGENTSLIST = "-QXstmz{"; //$NON-NLS-1$
	/**  select only paste agents  */
	public static final String DFLAGPAT_PASTEAGENTS = "+I"; //$NON-NLS-1$
	/**  display only database global script libraries  */
	public static final String DFLAGPAT_SCRIPTLIB = "+sh."; //$NON-NLS-1$
	/**  display only database global LotusScript script libraries  */
	public static final String DFLAGPAT_SCRIPTLIB_LS = "(+s-jh.*"; //$NON-NLS-1$
	/**  display only database global Java script libraries  */
	public static final String DFLAGPAT_SCRIPTLIB_JAVA = "*sj"; //$NON-NLS-1$
	/**  display only database global Javascript script libraries  */
	public static final String DFLAGPAT_SCRIPTLIB_JS = "+h"; //$NON-NLS-1$
	/**  display only database global JS server side script libraries  */
	public static final String DFLAGPAT_SCRIPTLIB_SERVER_JS = "+."; //$NON-NLS-1$
	/**  display only database level script  */
	public static final String DFLAGPAT_DATABASESCRIPT = "+t"; //$NON-NLS-1$

	/**  display only subforms; version filtering	 */
	public static final String DFLAGPAT_SUBFORM = "(+U-40n*"; //$NON-NLS-1$
	/**  display only subforms; for design mode, version filtering	 */
	public static final String DFLAGPAT_SUBFORM_DESIGN = "(+U-40*"; //$NON-NLS-1$
	/**  only subforms; no version filtering  */
	public static final String DFLAGPAT_SUBFORM_ALL_VERSIONS = "+U"; //$NON-NLS-1$
	/**  run all background filters  */
	public static final String DFLAGPAT_DBRUNMACRO = "+BS"; //$NON-NLS-1$
	/**  display forms that belong in compose menu; version filtering  */
	public static final String DFLAGPAT_COMPOSE = "-C40n"; //$NON-NLS-1$
	/**  select elements not hidden from notes  */
	public static final String DFLAGPAT_NOHIDDENNOTES = "-n"; //$NON-NLS-1$
	/**  select elements not hidden from web  */
	public static final String DFLAGPAT_NOHIDDENWEB = "-w"; //$NON-NLS-1$
	/**  display forms that appear in query by form; version filtering  */
	public static final String DFLAGPAT_QUERYBYFORM = "-DU40gnyz{:|"; //$NON-NLS-1$
	/**  related to data dictionary; no version filtering  */
	public static final String DFLAGPAT_PRESERVE = "+P"; //$NON-NLS-1$
	/**  subforms in the add subform list; no version filtering  */
	public static final String DFLAGPAT_SUBADD = "(+-40*UA"; //$NON-NLS-1$
	/**  subforms that are listed when making a new form. */
	public static final String DFLAGPAT_SUBNEW = "(+-40*UN"; //$NON-NLS-1$
	/**  display only views  */
	public static final String DFLAGPAT_VIEW = "-FG40n^"; //$NON-NLS-1$
	/**  display things showable on the menu  */
	public static final String DFLAGPAT_VIEW_MENUABLE = "-FQMUGXWy#i@40nK;g~%z^}"; //$NON-NLS-1$
	/**  display only views (not folders, navigators or shared columns)  */
	public static final String DFLAGPAT_VIEW_ALL_VERSIONS = "-FG^"; //$NON-NLS-1$
	/**  display only views, ignore hidden from notes  */
	public static final String DFLAGPAT_VIEW_DESIGN = "-FG40^"; //$NON-NLS-1$
	/**  design element is not hidden */
	public static final String DFLAGPAT_NOTHIDDEN = "-40n"; //$NON-NLS-1$
	/**  display only folders; version filtering  */
	public static final String DFLAGPAT_FOLDER = "(+-04n*F"; //$NON-NLS-1$
	/**  display only folders; version filtering, ignore hidden notes  */
	public static final String DFLAGPAT_FOLDER_DESIGN = "(+-04*F"; //$NON-NLS-1$
	/**  display only folders; no version filtering (for design)  */
	public static final String DFLAGPAT_FOLDER_ALL_VERSIONS = "*F"; //$NON-NLS-1$
	/**  display only calendar-style views  */
	public static final String DFLAGPAT_CALENDAR = "*c"; //$NON-NLS-1$
	/**  display only shared views  */
	public static final String DFLAGPAT_SHAREDVIEWS = "-FGV^40n"; //$NON-NLS-1$
	/**  display only shared views and folder; all notes & web  */
	public static final String DFLAGPAT_SHAREDVIEWSFOLDERS = "-G^V40p"; //$NON-NLS-1$
	/**  display only shared views not hidden from web  */
	public static final String DFLAGPAT_SHAREDWEBVIEWS = "-FGV40wp^"; //$NON-NLS-1$
	/**  display only shared views and folders not hidden from web  */
	public static final String DFLAGPAT_SHAREDWEBVIEWSFOLDERS = "-GV40wp^"; //$NON-NLS-1$
	/**  display only views and folder; version filtering  */
	public static final String DFLAGPAT_VIEWS_AND_FOLDERS = "-G40n^"; //$NON-NLS-1$
	/**  display only views and folder; all notes & web  */
	public static final String DFLAGPAT_VIEWS_AND_FOLDERS_DESIGN = "-G40^"; //$NON-NLS-1$
	/**  display only shared columns  */
	public static final String DFLAGPAT_SHARED_COLS = "(+-*^"; //$NON-NLS-1$

	/**  display only GraphicViews; version filtering  */
	public static final String DFLAGPAT_VIEWMAP = "(+-04n*G"; //$NON-NLS-1$
	/**  display only GraphicViews; no version filtering (for design)  */
	public static final String DFLAGPAT_VIEWMAP_ALL_VERSIONS = "*G"; //$NON-NLS-1$
	/**  display only GraphicViews available to web; version filtering  */
	public static final String DFLAGPAT_VIEWMAPWEB = "(+-04w*G"; //$NON-NLS-1$
	/**  display only GraphicViews; all notes & web navs  */
	public static final String DFLAGPAT_VIEWMAP_DESIGN = "(+-04*G"; //$NON-NLS-1$

	/**  display WebPages	 */
	public static final String DFLAGPAT_WEBPAGE = "(+-*W"; //$NON-NLS-1$
	/**  display WebPages	available to notes client  */
	public static final String DFLAGPAT_WEBPAGE_NOTES = "(+W-n*"; //$NON-NLS-1$
	/**  display WebPages	available to web client  */
	public static final String DFLAGPAT_WEBPAGE_WEB = "(+W-w*"; //$NON-NLS-1$
	/**  display forms that belong in compose menu  */
	public static final String DFLAGPAT_OTHER_DLG = "(+-04n*H"; //$NON-NLS-1$
	/**  display only categorized views  */
	public static final String DFLAGPAT_CATEGORIZED_VIEW = "(+-04n*T"; //$NON-NLS-1$

	/**  detect default design note for it's class (used for VIEW)  */
	public static final String DFLAGPAT_DEFAULT_DESIGN = "+d"; //$NON-NLS-1$
	/**  display only Frameset notes  */
	public static final String DFLAGPAT_FRAMESET = "(+-*#"; //$NON-NLS-1$
	/**  Frameset notes available to notes client  */
	public static final String DFLAGPAT_FRAMESET_NOTES = "(+#-n*"; //$NON-NLS-1$
	/**  Frameset notes available to web client  */
	public static final String DFLAGPAT_FRAMESET_WEB = "(+#-w*"; //$NON-NLS-1$
	/**  SiteMap notes (actually, "mQ345")  */
	public static final String DFLAGPAT_SITEMAP = "+m"; //$NON-NLS-1$
	/**  sitemap notes available to notes client  */
	public static final String DFLAGPAT_SITEMAP_NOTES = "(+m-n*"; //$NON-NLS-1$
	/**  sitemap notes available to web client  */
	public static final String DFLAGPAT_SITEMAP_WEB = "(+m-w*"; //$NON-NLS-1$
	/**  display only shared image resources  */
	public static final String DFLAGPAT_IMAGE_RESOURCE = "+i"; //$NON-NLS-1$
	/**  display only notes visible images  */
	public static final String DFLAGPAT_IMAGE_RES_NOTES = "(+i-n~*"; //$NON-NLS-1$
	/**  display only web visible images  */
	public static final String DFLAGPAT_IMAGE_RES_WEB = "(+i-w~*"; //$NON-NLS-1$
	/**  display only shared image resources that 
													have more than one image across  */
	public static final String DFLAGPAT_IMAGE_WELL_RESOURCE = "(+-*iv"; //$NON-NLS-1$
	/**  display only shared image resources that 
													have more than one image across - notes only  */
	public static final String DFLAGPAT_IMAGE_WELL_NOTES = "(+-n*iv"; //$NON-NLS-1$
	/**  display only shared image resources that 
													have more than one image across - web only  */
	public static final String DFLAGPAT_IMAGE_WELL_WEB = "(+-w*iv"; //$NON-NLS-1$
	/**  display only java design elements  */
	public static final String DFLAGPAT_JAVAFILE = "(+-*g["; //$NON-NLS-1$

	/**  display only shared Java resources  */
	public static final String DFLAGPAT_JAVA_RESOURCE = "+@"; //$NON-NLS-1$
	/**  display only shared Java resources visible to notes  */
	public static final String DFLAGPAT_JAVA_RESOURCE_NOTES = "(+@-n*"; //$NON-NLS-1$
	/**  display only shared Java resources visible to web  */
	public static final String DFLAGPAT_JAVA_RESOURCE_WEB = "(+@-w*"; //$NON-NLS-1$

	/**  xsp pages  */
	public static final String DFLAGPAT_XSPPAGE = "*gK"; //$NON-NLS-1$
	/**  xsp pages for the web  */
	public static final String DFLAGPAT_XSPPAGE_WEB = "(+-w*gK"; //$NON-NLS-1$
	/**  xsp pages for Notes  */
	public static final String DFLAGPAT_XSPPAGE_NOTES = "(+-n*gK"; //$NON-NLS-1$

	/**  xsp pages, no prop files  */
	public static final String DFLAGPAT_XSPPAGE_NOPROPS = "(+-2*gK"; //$NON-NLS-1$
	/**  xsp pages, no prop files, for the web  */
	public static final String DFLAGPAT_XSPPAGE_NOPROPS_WEB = "(+-2w*gK"; //$NON-NLS-1$
	/**  xsp pages, no prop files, for Notes  */
	public static final String DFLAGPAT_XSPPAGE_NOPROPS_NOTES = "(+-2n*gK"; //$NON-NLS-1$

	/**  xsp pages  */
	public static final String DFLAGPAT_XSPCC = "*g;"; //$NON-NLS-1$
	/**  xsp pages for the web  */
	public static final String DFLAGPAT_XSPCC_WEB = "(+-w*g;"; //$NON-NLS-1$
	/**  xsp pages for Notes  */
	public static final String DFLAGPAT_XSPCC_NOTES = "(+-n*g;"; //$NON-NLS-1$

	/**  display only shared data connection resources  */
	public static final String DFLAGPAT_DATA_CONNECTION_RESOURCE = "+k"; //$NON-NLS-1$
	/**  display only db2 access views  */
	public static final String DFLAGPAT_DB2ACCESSVIEW = "+z"; //$NON-NLS-1$

	/**  display only shared style sheet resources  */
	public static final String DFLAGPAT_STYLE_SHEET_RESOURCE = "+="; //$NON-NLS-1$
	/**  display only notes visible style sheets  */
	public static final String DFLAGPAT_STYLE_SHEETS_NOTES = "(+=-n*"; //$NON-NLS-1$
	/**  display only web visible style sheets  */
	public static final String DFLAGPAT_STYLE_SHEETS_WEB = "(+=-w*"; //$NON-NLS-1$
	/**  display only files  */
	public static final String DFLAGPAT_FILE = "+g-K[];`,"; //$NON-NLS-1$
	/**  list of files that should show in file DL  */
	public static final String DFLAGPAT_FILE_DL = "(+g-~K[];`,*"; //$NON-NLS-1$
	/**  list of notes only files  */
	public static final String DFLAGPAT_FILE_NOTES = "(+g-K[];n`,*"; //$NON-NLS-1$
	/**  list of web only files  */
	public static final String DFLAGPAT_FILE_WEB = "(+g-K[];w`,*"; //$NON-NLS-1$
	/**  display only html files  */
	public static final String DFLAGPAT_HTMLFILES = "(+-*g>"; //$NON-NLS-1$
	/**  htmlfiles that are notes visible  */
	public static final String DFLAGPAT_HTMLFILES_NOTES = "(+-n*g>"; //$NON-NLS-1$
	/**  htmlfiles that are web visible  */
	public static final String DFLAGPAT_HTMLFILES_WEB = "(+-w*g>"; //$NON-NLS-1$
	/**  files plus images plus comp app plus style sheets with no directory elements, java elements  */
	public static final String DFLAGPAT_FILE_ELEMS = "(+gi|=-/[],*"; //$NON-NLS-1$

	/**  servlets  */
	public static final String DFLAGPAT_SERVLET = "+z"; //$NON-NLS-1$
	/**  servlets not hidden from notes  */
	public static final String DFLAGPAT_SERVLET_NOTES = "(+z-n*"; //$NON-NLS-1$
	/**  servlets not hidden from the web  */
	public static final String DFLAGPAT_SERVLET_WEB = "(+z-w*"; //$NON-NLS-1$

	/**  web service  */
	public static final String DFLAGPAT_WEBSERVICE = "+{"; //$NON-NLS-1$
	/**  java web services  */
	public static final String DFLAGPAT_JAVA_WEBSERVICE = "(+Jj-*{"; //$NON-NLS-1$
	/**  lotusscript web services  */
	public static final String DFLAGPAT_LS_WEBSERVICE = "*{L"; //$NON-NLS-1$

	/**  display only JSP's  */
	public static final String DFLAGPAT_JSP = "(+-*g<"; //$NON-NLS-1$

	/**  display only stylekits  */
	public static final String DFLAGPAT_STYLEKIT = "(+-*g`"; //$NON-NLS-1$
	/**  display only notes client stylekits  */
	public static final String DFLAGPAT_STYLEKIT_NOTES = "(+-n*g`"; //$NON-NLS-1$
	/**  display only web client stylekits  */
	public static final String DFLAGPAT_STYLEKIT_WEB = "(+-w*g`"; //$NON-NLS-1$

	/**  display only widgets  */
	public static final String DFLAGPAT_WIDGET = "(+-*g_"; //$NON-NLS-1$
	/**  display only notes client stylekits  */
	public static final String DFLAGPAT_WIDGET_NOTES = "(+-n*g_"; //$NON-NLS-1$
	/**  display only web client stylekits  */
	public static final String DFLAGPAT_WIDGET_WEB = "(+-w*g_"; //$NON-NLS-1$
	
	public static final String DFLAGPAT_SACTIONS_DESIGN = "+y"; //$NON-NLS-1$
	public static final String DFLAGPAT_SACTIONS_WEB = "(+-0*y"; //$NON-NLS-1$
	public static final String DFLAGPAT_SACTIONS_NOTES = "(+-0*y"; //$NON-NLS-1$

	/** Wiring Properties element is a Form note. LI 3925.05 */
	public static final String DFLAGPAT_COMPDEF = "+:"; //$NON-NLS-1$
	/** Composite Application element is a Form note. LI 3925.04 */
	public static final String DFLAGPAT_COMPAPP = "+|"; //$NON-NLS-1$
}
