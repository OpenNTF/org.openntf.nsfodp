/*
 * � Copyright IBM Corp. 2011
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package com.ibm.xsp.extlib.designer.xspprops;


/**
 * @author mleland
 *
 */
public interface XSPAllPropertyConstants {
    public static final String XSP_PERSIST_THRESHHOLD = "xsp.persistence.file.threshold"; //$NON-NLS-1$
    public static final String XSP_FORCE_FULLREFRESH = "xsp.application.forcefullrefresh";	//$NON-NLS-1$
    
    String XSP_LIBRARY_DEPENDENCIES = "xsp.library.depends"; //$NON-NLS-1$
    String XSP_PERSISTENCE_MODE = "xsp.persistence.mode"; //$NON-NLS-1$
    String XSP_PERSISTENCE_FILE = "file"; //$NON-NLS-1$
    String XSP_AJAX_WHOLE_TREE_RENDER = "xsp.ajax.renderwholetree"; //$NON-NLS-1$
    String WHOLE_TREE_RENDER_DEFVAL = "false"; //$NON-NLS-1$
    String XSP_AGGREGATE_RESOURCES = "xsp.resources.aggregate"; //$NON-NLS-1$
    String XSP_AGGREGATE_RESOURCE_DEFNEWVAL = "true"; //$NON-NLS-1$
    String ERROR_PAGE = "xsp.error.page"; //$NON-NLS-1$
    String DEFAULT_ERROR_PAGE = "xsp.error.page.default"; //$NON-NLS-1$
    String DEFAULT_ERROR_PAGE_DEFVAL = "false"; //$NON-NLS-1$
    String APP_TIMEOUT = "xsp.application.timeout"; //$NON-NLS-1$
    String SESSION_TIMEOUT = "xsp.session.timeout"; //$NON-NLS-1$
    String FILE_UPLOAD_MAXSIZE = "xsp.upload.maximumsize"; //$NON-NLS-1$
    String FILE_UPLOAD_DIRECTORY = "xsp.upload.directory"; //$NON-NLS-1$
    String PAGE_ENCODING = "xsp.html.page.encoding"; //$NON-NLS-1$
    String PAGE_COMPRESS = "xsp.compress.mode"; //$NON-NLS-1$
    String CLIENT_VALIDATE = "xsp.client.validation"; //$NON-NLS-1$
    String JSCRIPT_CACHESIZE = "ibm.jscript.cachesize"; //$NON-NLS-1$
    String JSCRIPT_CACHESIZE_DEFVAL = "400"; //$NON-NLS-1$
    String USER_TIMEZONE = "xsp.user.timezone"; //$NON-NLS-1$
    String DB_THEME = "xsp.theme"; //$NON-NLS-1$
    String DB_THEME_NOTES = "xsp.theme.notes"; //$NON-NLS-1$
    String DB_THEME_WEB = "xsp.theme.web"; //$NON-NLS-1$
    String DB_MOBILE_THEME = "xsp.theme.mobile"; //$NON-NLS-1$
    String DB_MOBILE_THEME_IOS = "xsp.theme.mobile.iphone"; //$NON-NLS-1$
    String DB_MOBILE_THEME_ANDROID = "xsp.theme.mobile.android"; //$NON-NLS-1$
    String DB_MOBILE_DEBUG_USER_AGENT = "xsp.theme.mobile.debug.userAgent"; //$NON-NLS-1$
    String XSP_APP_DEFAULT_LINK_TARGET = "xsp.default.link.target"; //$NON-NLS-1$
    String XSP_AGGREGATE_RESOURCE_DEFVAL = "false"; //$NON-NLS-1$
    String XSP_HTML_DOCTYPE = "xsp.html.doctype"; //$NON-NLS-1$
    String XSP_MINIMUM_VERSION_LEVEL = "xsp.min.version"; //$NON-NLS-1$
    String XSP_SAVE_LINKS = "xsp.save.links"; //$NON-NLS-1$
    String XSP_SAVE_USE_NOTES = "UseNotes"; //$NON-NLS-1$
    String XSP_SAVE_USE_WEB = "UseWeb"; //$NON-NLS-1$
    String XSP_CLIENT_DOJO_VSN = "xsp.client.script.dojo.version"; //$NON-NLS-1$
    String XSP_DOJO_CONFIG = "xsp.client.script.dojo.djConfig"; //$NON-NLS-1$
    String XSP_EXPIRES_GLOBAL = "xsp.expires.global"; //$NON-NLS-1$
    String XSP_HTML_PREFERRED_CT = "xsp.html.preferredcontenttypexhtml"; //$NON-NLS-1$
    String XSP_HTMLFILTER_ACF = "xsp.htmlfilter.acf.config"; //$NON-NLS-1$
    String XSP_PERSISTENCE_GZIP = "xsp.persistence.file.gzip"; //$NON-NLS-1$
    String XSP_PERS_DIR_XSPPERS = "xsp.persistence.dir.xsppers"; //$NON-NLS-1$
    String XSP_PERS_DIR_XSPSTATE = "xsp.persistence.dir.xspstate"; //$NON-NLS-1$
    String XSP_PERS_DIR_XSPUPLOAD = "xsp.persistence.dir.xspupload"; //$NON-NLS-1$
    String XSP_PERS_DISCARDJS = "xsp.persistence.discardjs"; //$NON-NLS-1$
    String XSP_PERS_FILE_ASYNC = "xsp.persistence.file.async"; //$NON-NLS-1$
    String XSP_PERS_FILE_MAXVIEWS = "xsp.persistence.file.maxviews"; //$NON-NLS-1$
    String XSP_PERS_TREE_MAXVIEWS = "xsp.persistence.tree.maxviews"; //$NON-NLS-1$
    String XSP_PERS_VIEWSTATE = "xsp.persistence.viewstate"; //$NON-NLS-1$
    String XSP_REDIRECT = "xsp.redirect"; //$NON-NLS-1$
    String XSP_REPEAT_ALLOWZERO = "xsp.repeat.allowZeroRowsPerPage"; //$NON-NLS-1$
    String XSP_RT_DEF_HTMLFILTER = "xsp.richtext.default.htmlfilter"; //$NON-NLS-1$
    String XSP_RT_DEF_HTMLFILTERIN = "xsp.richtext.default.htmlfilterin"; //$NON-NLS-1$
    String XSP_SESSION_TRANSIENT = "xsp.session.transient"; //$NON-NLS-1$
    String XSP_THEME_NOCOMPDS = "xsp.theme.preventCompositeDataStyles"; //$NON-NLS-1$
    String XSP_USER_TZ_RT = "xsp.user.timezone.roundtrip"; //$NON-NLS-1$
    String XPATH_CACHESIZE = "ibm.xpath.cachesize"; //$NON-NLS-1$
    String XPATH_CACHESIZE_DEFVAL = "400"; //$NON-NLS-1$
    String XSP_PARTIAL_UPDATE_TIMEOUT = "xsp.partial.update.timeout"; //$NON-NLS-1$
    String XSP_HTML_METACONTENT = "xsp.html.meta.contenttype"; //$NON-NLS-1$
    String XSP_MOBILE_THEME = "xsp.theme.mobile.pagePrefix"; //$NON-NLS-1$
    String XSP_SEARCH_BOT_ID_LIST = "xsp.search.bot.id.list"; //$NON-NLS-1$
}