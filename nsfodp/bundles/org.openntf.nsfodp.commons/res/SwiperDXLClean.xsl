<?xml version="1.0" encoding="UTF-8"?>
<!--

Copyright 2013 Cameron Gregor
Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License

-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:n="http://www.lotus.com/dxl">
  <!-- Indent the result tree for more consistency when doing a diff - and it looks much nicer :) -->
  <xsl:output indent="yes"/>
  <!-- Strip whitespace so that when we remove elements it does not leave ugly blank gaps -->
  <xsl:strip-space elements="*"/>

<!-- 
  Start: Removal templates
    
    Each of the following templates are designed to match an element or attribute that 
    we do not want in the result tree.

-->

    <!-- We Don't want the NoteInfo element. We don't care who updated or signed the element  -->
    <xsl:template match="n:noteinfo|n:updatedby|n:wassignedby"/>

    <!-- For binary DXL elements replicaid and version are stored on a <note> element -->
    <xsl:template match="//n:note/@replicaid"/>
    <xsl:template match="//n:note/@version"/>

    <!-- I don't know what this is but it started showing up in 9.0.1 ?? -->
    <xsl:template match="//n:note/@maintenanceversion"/>
    <xsl:template match="@maintenanceversion"/>

    <!-- 
         The following templates cover the replicaid, version and designerversion
         attributes on the Standard DXL elements.
    -->

    <xsl:template match="//n:form/@replicaid"/>
    <xsl:template match="//n:form/@version"/>
    <xsl:template match="//n:form/@designerversion"/>

    <xsl:template match="//n:database/@replicaid"/>
    <xsl:template match="//n:database/@version"/>
    <xsl:template match="//n:database/@designerversion"/>

    <xsl:template match="//n:sharedactions/@replicaid"/>
    <xsl:template match="//n:sharedactions/@version"/>
    <xsl:template match="//n:sharedactions/@designerversion"/>

    <xsl:template match="//n:agent/@replicaid"/>
    <xsl:template match="//n:agent/@version"/>
    <xsl:template match="//n:agent/@designerversion"/>

    <xsl:template match="//n:scriptlibrary/@replicaid"/>
    <xsl:template match="//n:scriptlibrary/@version"/>
    <xsl:template match="//n:scriptlibrary/@designerversion"/>

    <xsl:template match="//n:databasescript/@replicaid"/>
    <xsl:template match="//n:databasescript/@version"/>
    <xsl:template match="//n:databasescript/@designerversion"/>

    <xsl:template match="//n:dataconnection/@replicaid"/>
    <xsl:template match="//n:dataconnection/@version"/>    
    <xsl:template match="//n:dataconnection/@designerversion"/>

    <xsl:template match="//n:folder/@replicaid"/>
    <xsl:template match="//n:folder/@version"/>
    <xsl:template match="//n:folder/@designerversion"/>

    <xsl:template match="//n:frameset/@replicaid"/>
    <xsl:template match="//n:frameset/@version"/>
    <xsl:template match="//n:frameset/@designerversion"/>

    <xsl:template match="//n:page/@replicaid"/>
    <xsl:template match="//n:page/@version"/>
    <xsl:template match="//n:page/@designerversion"/>

    <xsl:template match="//n:imageresource/@replicaid"/>
    <xsl:template match="//n:imageresource/@version"/>
    <xsl:template match="//n:imageresource/@designerversion"/>

    <xsl:template match="//n:helpaboutdocument/@replicaid"/>
    <xsl:template match="//n:helpaboutdocument/@version"/>
    <xsl:template match="//n:helpaboutdocument/@designerversion"/>

    <xsl:template match="//n:stylesheetresource/@replicaid"/>
    <xsl:template match="//n:stylesheetresource/@version"/>
    <xsl:template match="//n:stylesheetresource/@designerversion"/>

    <xsl:template match="//n:view/@replicaid"/>
    <xsl:template match="//n:view/@version"/>
    <xsl:template match="//n:view/@designerversion"/>

    <xsl:template match="//n:helpusingdocument/@replicaid"/>
    <xsl:template match="//n:helpusingdocument/@version"/>
    <xsl:template match="//n:helpusingdocument/@designerversion"/>

    <xsl:template match="//n:sharedcolumn/@replicaid"/>
    <xsl:template match="//n:sharedcolumn/@version"/>
    <xsl:template match="//n:sharedcolumn/@designerversion"/>

    <xsl:template match="//n:sharedfield/@replicaid"/>
    <xsl:template match="//n:sharedfield/@version"/>
    <xsl:template match="//n:sharedfield/@designerversion"/>

    <xsl:template match="//n:navigator/@replicaid"/>
    <xsl:template match="//n:navigator/@version"/>
    <xsl:template match="//n:navigator/@designerversion"/>

    <xsl:template match="//n:outline/@replicaid"/>
    <xsl:template match="//n:outline/@version"/>
    <xsl:template match="//n:outline/@designerversion"/>

    <xsl:template match="//n:subform/@replicaid"/>
    <xsl:template match="//n:subform/@version"/>
    <xsl:template match="//n:subform/@designerversion"/>

    <xsl:template match="//n:fileresource/@replicaid"/>
    <xsl:template match="//n:fileresource/@version"/>
    <xsl:template match="//n:fileresource/@designerversion"/>

    <!-- END Standard DXL replicaid, version, designerversion templates -->
    
    <!-- 
         For Agent Non-Binary DXL 
         For both LotusScript and Java agents
      
         For Java Agents You may also wish to look at some extra ones like javaproject->codepath or
         item->$JavaCompilerSource item->$JavaComplierTarget

    -->
    <xsl:template match="//n:agent/n:rundata"/>
    <xsl:template match="//n:agent/n:designchange"/>

    <xsl:template match="//n:javaproject/@codepath"/>

    <!-- not 100% but I don't like the sound of These! Not in the DTD in help anyway -->
    <xsl:template match="//n:folder/@formatnoteid"/> 
    <xsl:template match="//n:view/@formatnoteid"/> 


    <!-- 
        For the Database Properties Non-Binary DXL.
        Most of these attributes/elements are guaranteed to be different on different developer copies
    -->
    <xsl:template match="//n:database/@path"/>
    <xsl:template match="//n:database/n:databaseinfo/@dbid"/>
    <xsl:template match="//n:database/n:databaseinfo/@percentused"/>
    <xsl:template match="//n:database/n:databaseinfo/@numberofdocuments"/>
    <xsl:template match="//n:database/n:databaseinfo/@diskspace"/>
    <xsl:template match="//n:database/n:databaseinfo/@odsversion"/>
    <xsl:template match="//n:database/n:databaseinfo/n:datamodified"/>
    <xsl:template match="//n:database/n:databaseinfo/n:designmodified"/>
    
    <!--
        Clean log entries out of the ACL
    -->
    <xsl:template match="//n:database/n:acl/n:logentry"/>
    
    <!--
        Strip ephemeral/machine-specific items from icon notes
    -->
    <!-- TODO figure out why leaving the "/*" out doesn't match the node -->
    <xsl:template match="//n:note[@class='icon']/n:item[@name='$PIRCRefreshModTime']/*"/>
    <xsl:template match="//n:note[@class='icon']/n:item[@name='$TemplateModTime']/*"/>
    <xsl:template match="//n:note[@class='icon']/n:item[@name='$TemplateFileName']/*"/>
    
    <!--
        Clean mutable parts of fulltextsettings 
    -->
    <xsl:template match="//n:database/n:fulltextsettings/@size"/>
    <xsl:template match="//n:database/n:fulltextsettings/@unindexeddocumentcount"/>
    <xsl:template match="//n:database/n:fulltextsettings/n:lastindexed"/>

    <!-- 
         Remove any items that begin with $ and end with _O
         for example
         <item name="$yourfield_O" ....></item>

         These Items are Script Object items, they are not source code!
         you freshly check out a repo version of the design element, but at
         least you won't get merge conflicts all the time

         These items will come back to the Design Element after a recompile, they
         just won't end up in your repository, which is good news, because they are like .class files.
     -->
     <xsl:template match="//n:item">
       <xsl:if test="not(starts-with(@name,'$') and substring(@name,string-length(@name)-1,2) = '_O')">
        <xsl:call-template name="identity"/>
      </xsl:if>
    </xsl:template> 

    <!-- Ignore the DesignerVersion Item  and this random FileModDT one -->
    <xsl:template match="//n:item[@name='$DesignerVersion']"/>
    <xsl:template match="//n:item[@name='$$ScriptName']"/>
    <xsl:template match="//n:item[@name='$ScriptLib_error']"/>
    <xsl:template match="//n:imageresource/n:item[@name='$FileModDT']"/>
    <xsl:template match="//n:imageresource/n:item[@name='$EditFilePath']"/>

    <!-- 
         For any node not specified in one of the above templates, 
         simply copy it to the result tree.
         This template is also named so it can be called by call-template
    -->
    <xsl:template match="node() | @*" name="identity">
        <xsl:copy>
            <xsl:apply-templates select="node() | @*"/>
        </xsl:copy>
    </xsl:template> 
    
</xsl:stylesheet>

