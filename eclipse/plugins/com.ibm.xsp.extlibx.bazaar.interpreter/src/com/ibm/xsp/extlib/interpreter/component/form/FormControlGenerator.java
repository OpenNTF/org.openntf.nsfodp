/*
 * © Copyright IBM Corp. 2010
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

package com.ibm.xsp.extlib.interpreter.component.form;

import java.util.List;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.actions.ActionGroup;
import com.ibm.xsp.actions.OpenPageAction;
import com.ibm.xsp.actions.SaveDocumentAction;
import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.binding.ValueBindingEx;
import com.ibm.xsp.complex.Parameter;
import com.ibm.xsp.component.UIPanelEx;
import com.ibm.xsp.component.xp.XspCommandButton;
import com.ibm.xsp.component.xp.XspDateTimeHelper;
import com.ibm.xsp.component.xp.XspEventHandler;
import com.ibm.xsp.component.xp.XspInputRichText;
import com.ibm.xsp.component.xp.XspInputText;
import com.ibm.xsp.component.xp.XspInputTextarea;
import com.ibm.xsp.convert.DateTimeConverter;
import com.ibm.xsp.convert.NumberConverter;
import com.ibm.xsp.extlib.actions.server.ChangeDynamicContentAction;
import com.ibm.xsp.extlib.builder.ControlBuilder.ControlImpl;
import com.ibm.xsp.extlib.builder.ControlBuilder.IControl;
import com.ibm.xsp.extlib.component.data.UIFormLayoutRow;
import com.ibm.xsp.extlib.component.data.UIFormTable;
import com.ibm.xsp.extlib.component.picker.UINamePicker;
import com.ibm.xsp.extlib.component.picker.data.DominoNABNamePickerData;


/**
 * Form Control Generator.
 * <p>
 * This generator is used to generate the XPages controls from a high level form definition.
 * </p>
 * @author priand
 */
public class FormControlGenerator {
	
	private FormControlBuilder builder;
	
	private String dataSource;
	
	protected FormControlGenerator(FormControlBuilder builder) {
		this.builder= builder;
		this.dataSource =builder.getDataSource();
		if(StringUtil.isEmpty(dataSource)) {
			this.dataSource = com.ibm.xsp.model.domino.DatabaseConstants.CURRENT_DOCUMENT;
		}
	}

	public FormControlBuilder getBuilder() {
		return builder;
	}
	
	public IControl generateXPagesControl(CForm form) {
		ControlImpl<UIFormTable> c = createFormTable(form);
		generateRows(form,c);
		generateActions(form,c);
		return c;
	}
	
	protected void generateRows(CForm form, ControlImpl<UIFormTable> formTable) {
		List<CFormRow> rows = form.getRows();
		if(rows!=null) {
			int index = 0;
			for(CFormRow row: rows) {
				generateRow(form, row, formTable, index++);
			}
		}
	}
	protected void generateRow(CForm form, CFormRow row, ControlImpl<UIFormTable> formTable, int index) {
		ControlImpl<UIFormLayoutRow> formRow = new ControlImpl<UIFormLayoutRow>(new UIFormLayoutRow());
		formTable.addChild(formRow);
		formRow.getComponent().setLabel(row.getLabel());
		generateEditControl(form, row, formRow, index);
	}
	protected void generateActions(CForm form, ControlImpl<UIFormTable> formTable) {
		String actions = form.getActions();
		if(StringUtil.isNotEmpty(actions)) {
			generateActions(form, formTable, actions);
		} else {
			generateActions(form, formTable, "save|cancel");
		}
	}
	protected void generateActions(CForm form, ControlImpl<UIFormTable> formTable, String actions) {
		ControlImpl<UIPanelEx> pnl = new ControlImpl<UIPanelEx>(new UIPanelEx());
		formTable.putFacet("footer",pnl);

		String[] actionsArray = StringUtil.splitString(actions, '|');
		for(int i=0; i<actionsArray.length; i++) {
			String a = actionsArray[i];
			if(a.equalsIgnoreCase(CForm.ACTIONS_SAVE)) {
				generateActionSave(form,formTable,pnl);
			} else if(a.equalsIgnoreCase(CForm.ACTIONS_SAVEASDRAFT)) {
				generateActionSaveAsDraft(form,formTable,pnl);
			} else if(a.equalsIgnoreCase(CForm.ACTIONS_CANCEL)) {
				generateActionCancel(form,formTable,pnl);
			} else if(a.equalsIgnoreCase(CForm.ACTIONS_DELETE)) {
				generateActionDelete(form,formTable,pnl);
			}
		}
	}
	protected void generateActionSave(CForm form, ControlImpl<UIFormTable> formTable, ControlImpl<UIPanelEx> pnl) {
		ControlImpl<XspCommandButton> b = createButton(form);
		b.getComponent().setId("btSave");
		b.getComponent().setValue("Save");
		ControlImpl<XspEventHandler> e = createEventHandler(form);
		initButtonEventHandlerAction(form,e,true);
		b.addChild(e);
		pnl.addChild(b);
	}
	protected void generateActionSaveAsDraft(CForm form, ControlImpl<UIFormTable> formTable, ControlImpl<UIPanelEx> pnl) {
		ControlImpl<XspCommandButton> b = createButton(form);
		b.getComponent().setId("btSaveAsDraft");
		b.getComponent().setValue("Save As Draft");
		ControlImpl<XspEventHandler> e = createEventHandler(form);
		e.getComponent().setEvent("onclick");
		e.getComponent().setSubmit(true);
		e.getComponent().setSave(true);
		//e.getComponent().setAction(new ConstantMethodBinding("/Home.xsp"));
		b.addChild(e);
		pnl.addChild(b);
	}
	protected void generateActionCancel(CForm form, ControlImpl<UIFormTable> formTable, ControlImpl<UIPanelEx> pnl) {
		ControlImpl<XspCommandButton> b = createButton(form);
		b.getComponent().setId("btCancel");
		b.getComponent().setValue("Cancel");
		ControlImpl<XspEventHandler> e = createEventHandler(form);
		initButtonEventHandlerAction(form,e,false);
		e.getComponent().setImmediate(true);
		b.addChild(e);
		pnl.addChild(b);
	}
	protected void generateActionDelete(CForm form, ControlImpl<UIFormTable> formTable, ControlImpl<UIPanelEx> pnl) {
		ControlImpl<XspCommandButton> b = createButton(form);
		b.getComponent().setId("btDelete");
		b.getComponent().setValue("Delete");
		pnl.addChild(b);
	}
	
	protected void initButtonEventHandlerAction(CForm form, ControlImpl<XspEventHandler> e, boolean save) {
		e.getComponent().setEvent("onclick");
		e.getComponent().setSubmit(true);

		ActionGroup ac = new ActionGroup();
		ac.setComponent(e.getComponent());
		e.getComponent().setAction(ac);

		if(save) {
			SaveDocumentAction a = new SaveDocumentAction();
			ac.addAction(a);
		}
		
		String nextPage = builder.getNextPage();
		if(StringUtil.isNotEmpty(nextPage)) {
			if(nextPage.startsWith("#")) {
				ChangeDynamicContentAction a = new ChangeDynamicContentAction();
				a.setComponent(e.getComponent());
				a.setFacetName(nextPage.substring(1));
				String qs = builder.getQueryString();
				if(StringUtil.isNotEmpty(qs)) {
					String[] p = StringUtil.splitString(qs, '&');
					for(int i=0; i<p.length; i++) {
						String ps = p[i];
						int pos = ps.indexOf('=');
						if(pos>=0) {
							String name = ps.substring(0,pos);
							String value = ps.substring(pos+1);
							Parameter pa = new Parameter();
							pa.setName(name);
							pa.setValue(value);
							a.addParameter(pa);
						}
					}
				}
				ac.addAction(a);
			} else {
				OpenPageAction a = new OpenPageAction();
				a.setComponent(e.getComponent());
				a.setName(nextPage);
				String qs = builder.getQueryString();
				if(StringUtil.isNotEmpty(qs)) {
					String[] p = StringUtil.splitString(qs, '&');
					for(int i=0; i<p.length; i++) {
						String ps = p[i];
						int pos = ps.indexOf('=');
						if(pos>=0) {
							String name = ps.substring(0,pos);
							String value = ps.substring(pos+1);
							Parameter pa = new Parameter();
							pa.setName(name);
							pa.setValue(value);
							a.addParameter(pa);
						}
					}
				}
				ac.addAction(a);
			}
		}
	}
	
	
    protected ValueBinding createValueBinding(CForm form, UIComponent source, String expression, Class<?> expectedType) {
        Application app = FacesContext.getCurrentInstance().getApplication();
        javax.faces.el.ValueBinding vb = app.createValueBinding(expression);
        if(vb instanceof ValueBindingEx) {
            ValueBindingEx vbx = (ValueBindingEx)vb;
            if(source!=null) {
                vbx.setComponent(source);
            }
            if(expectedType!=null) {
            	vbx.setExpectedType(expectedType);
            }
        }
        return vb;
    }

	
	protected ControlImpl<UIFormTable> createFormTable(CForm form) {
		UIFormTable tb = new UIFormTable();
		tb.setFormTitle(form.getTitle());
		tb.setFormDescription(form.getDescription());
		return new ControlImpl<UIFormTable>(tb);
	}
	
	protected ControlImpl<UIFormLayoutRow> createFormRow(CForm form, CFormRow row) {
		UIFormLayoutRow r = new UIFormLayoutRow();
		return new ControlImpl<UIFormLayoutRow>(r);
	}

	protected ControlImpl<XspCommandButton> createButton(CForm form) {
		XspCommandButton b = new XspCommandButton();
		return new ControlImpl<XspCommandButton>(b);
	}	
	
	protected ControlImpl<XspEventHandler> createEventHandler(CForm form) {
		XspEventHandler b = new XspEventHandler();
		return new ControlImpl<XspEventHandler>(b);
	}	

	
	
	// ===========================================================================
	// Edit control creation
	// ===========================================================================
	
	protected void generateEditControl(CForm form, CFormRow row, ControlImpl<UIFormLayoutRow> formRow, int index) {
		String type = row.getType();
		if(StringUtil.isNotEmpty(type)) {
			ControlImpl<?> e = null; ControlImpl<?> ee = null;
			if(type.equals(CFormRow.TYPE_BOOLEAN)) {
			} else if(type.equals(CFormRow.TYPE_DATE)) {
				ControlImpl<XspInputText> c = createInputText(form,row); e=c;
				generateFieldId(e, index);
				DateTimeConverter converter = new DateTimeConverter();
				converter.setType("date");
				c.getComponent().setConverter(converter);
				ControlImpl<XspDateTimeHelper> dt = createDateTimeHelper(form,row);
				c.addChild(dt);
			} else if(type.equals(CFormRow.TYPE_DATETIME)) {
				ControlImpl<XspInputText> c = createInputText(form,row); e=c;
				generateFieldId(e, index);
				DateTimeConverter converter = new DateTimeConverter();
				converter.setType("both");
				c.getComponent().setConverter(converter);
				ControlImpl<XspDateTimeHelper> dt = createDateTimeHelper(form,row);
				c.addChild(dt);
			} else if(type.equals(CFormRow.TYPE_DROPDOWN)) {
			} else if(type.equals(CFormRow.TYPE_MULTILINETEXT)) {
				ControlImpl<XspInputTextarea>  c = createInputTextarea(form,row); e=c;
				generateFieldId(e, index);
			} else if(type.equals(CFormRow.TYPE_NUMBER)) {
				ControlImpl<XspInputText> c = createInputText(form,row); e=c;
				generateFieldId(e, index);
				NumberConverter converter = new NumberConverter();
				c.getComponent().setConverter(converter);
			} else if(type.equals(CFormRow.TYPE_PERSON)) {
				ControlImpl<XspInputText> c = createInputText(form,row); e=c;
				generateFieldId(e, index);
				ControlImpl<UINamePicker> p = createNamePicker(form, row); ee=p;
				p.getComponent().setDataProvider(new DominoNABNamePickerData());
				p.getComponent().setFor(e.getComponent().getId());
			} else if(type.equals(CFormRow.TYPE_PICKER)) {
				ControlImpl<XspInputText> c = createInputText(form,row); e=c;
				generateFieldId(e, index);
			} else if(type.equals(CFormRow.TYPE_RADIOBUTTONS)) {
			} else if(type.equals(CFormRow.TYPE_RICHTEXT)) {
				ControlImpl<XspInputRichText> c = createInputRichText(form,row); e=c;
				generateFieldId(e, index);
			} else if(type.equals(CFormRow.TYPE_TEXT)) {
				ControlImpl<XspInputText> c = createInputText(form,row); e=c;
				generateFieldId(e, index);
			} else if(type.equals(CFormRow.TYPE_TIME)) {
				ControlImpl<XspInputText> c = createInputText(form,row); e=c;
				generateFieldId(e, index);
				DateTimeConverter converter = new DateTimeConverter();
				converter.setType("time");
				c.getComponent().setConverter(converter);
				ControlImpl<XspDateTimeHelper> dt = createDateTimeHelper(form,row);
				c.addChild(dt);
			} else if(type.equals(CFormRow.TYPE_URL)) {
			}
			if(e!=null) {
				generateDataBinding(form,row,formRow,e); 
				formRow.addChild(e);
			}
			if(ee!=null) {
				String id = "flde"+(index+1);
				ee.getComponent().setId(id);
				formRow.addChild(ee);
			}
		}
	}
	private void generateFieldId(ControlImpl<?> e, int index) {
		String id = "fld"+(index+1);
		e.getComponent().setId(id);
	}
	
/*	
	<xp:inputText id="inputText5">
	<xp:typeAhead mode="full"
		minChars="1" preventFiltering="true">
		<xp:this.valueList><![CDATA[#{javascript:getComponent("valuePicker5").getTypeAheadValue(this)}]]></xp:this.valueList>
	</xp:typeAhead>
</xp:inputText>
<xe:valuePicker id="valuePicker5"
	for="inputText5" dojoType="extlib.dijit.PickerCheckbox">
	<xe:this.dataProvider>
		<xe:dominoViewValuePicker
			viewName="AllEMails" labelColumn="Name">
		</xe:dominoViewValuePicker>
	</xe:this.dataProvider>
</xe:valuePicker>
*/
	protected void generateDataBinding(CForm form, CFormRow row, ControlImpl<UIFormLayoutRow> formRow, ControlImpl<?> control) {
		UIComponent c = control.getComponent();
		if(c instanceof UIInput) {
			UIInput input = (UIInput)c;
			String s = "#{"+dataSource+"."+row.getField()+"}";
			ValueBinding vb = ApplicationEx.getInstance().createValueBinding(s);
			if(vb instanceof ValueBindingEx) {
				ValueBindingEx vbx = (ValueBindingEx)vb;
				vbx.setComponent(c);
                //vbx.setExpectedType(property.getJavaClass());
			}
			input.setValueBinding("value", vb);
		}
	}

	protected ControlImpl<XspInputText> createInputText(CForm form, CFormRow row) {
		XspInputText b = new XspInputText();
		int cols = row.getCols();
		if(cols>0) {
			b.setSize(cols);
		}
		return new ControlImpl<XspInputText>(b);
	}	
	protected ControlImpl<XspDateTimeHelper> createDateTimeHelper(CForm form, CFormRow row) {
		XspDateTimeHelper b = new XspDateTimeHelper();
		return new ControlImpl<XspDateTimeHelper>(b);
	}	
	protected ControlImpl<UINamePicker> createNamePicker(CForm form, CFormRow row) {
		UINamePicker b = new UINamePicker();
		return new ControlImpl<UINamePicker>(b);
	}	

	protected ControlImpl<XspInputTextarea> createInputTextarea(CForm form, CFormRow row) {
		XspInputTextarea b = new XspInputTextarea();
		int cols = row.getCols();
		if(cols>0) {
			b.setCols(cols);
		}
		int rows = row.getRows();
		if(rows>0) {
			b.setRows(rows);
		}
		return new ControlImpl<XspInputTextarea>(b);
	}	

	protected ControlImpl<XspInputRichText> createInputRichText(CForm form, CFormRow row) {
		XspInputRichText b = new XspInputRichText();
		return new ControlImpl<XspInputRichText>(b);
	}	
	
}
