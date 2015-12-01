/**
 * ************************************************************************
 * * The contents of this file are subject to the MRPL 1.2
 * * (the  "License"),  being   the  Mozilla   Public  License
 * * Version 1.1  with a permitted attribution clause; you may not  use this
 * * file except in compliance with the License. You  may  obtain  a copy of
 * * the License at http://www.floreantpos.org/license.html
 * * Software distributed under the License  is  distributed  on  an "AS IS"
 * * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * * License for the specific  language  governing  rights  and  limitations
 * * under the License.
 * * The Original Code is FLOREANT POS.
 * * The Initial Developer of the Original Code is OROCUBE LLC
 * * All portions are Copyright (C) 2015 OROCUBE LLC
 * * All Rights Reserved.
 * ************************************************************************
 */
/*
 * ModofierGroupEditor.java
 *
 * Created on August 4, 2006, 12:28 AM
 */

package com.floreantpos.ui.model;

import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.hibernate.Hibernate;
import org.hibernate.Session;

import com.floreantpos.POSConstants;
import com.floreantpos.model.MenuModifierGroup;
import com.floreantpos.model.dao.ModifierDAO;
import com.floreantpos.model.dao.ModifierGroupDAO;
import com.floreantpos.swing.FixedLengthTextField;
import com.floreantpos.swing.MessageDialog;
import com.floreantpos.ui.BeanEditor;
import com.floreantpos.util.POSUtil;

/**
 *
 * @author  MShahriar
 */
public class MenuModifierGroupForm extends BeanEditor {

	/** Creates new form ModofierGroupEditor */
	public MenuModifierGroupForm() throws Exception {
		this(new MenuModifierGroup());
	}

	public MenuModifierGroupForm(MenuModifierGroup group) throws Exception {
		initComponents();
		
		setBean(group);
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
    	setLayout(new MigLayout("", "[45px][369px,grow]", "[19px][]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
        jLabel1 = new javax.swing.JLabel();
        tfName = new com.floreantpos.swing.FixedLengthTextField();
        tfName.setLength(60);

        jLabel1.setText(com.floreantpos.POSConstants.NAME);

        add(jLabel1, "cell 0 0,alignx left,aligny center"); //$NON-NLS-1$
		add(tfName, "cell 1 0,growx,aligny top"); //$NON-NLS-1$
		
		JLabel lblTranslatedName = new JLabel(POSConstants.TRANSLATED_NAME);
		add(lblTranslatedName, "cell 0 1,alignx trailing"); //$NON-NLS-1$
		
		tfTranslatedName = new FixedLengthTextField();
		tfTranslatedName.setLength(60);
		add(tfTranslatedName, "cell 1 1,growx"); //$NON-NLS-1$
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private com.floreantpos.swing.FixedLengthTextField tfName;
    private FixedLengthTextField tfTranslatedName;
    // End of variables declaration//GEN-END:variables
	@Override
	public boolean save() {
		try {
			if(!updateModel()) return false;
			
			MenuModifierGroup group = (MenuModifierGroup) getBean();

			ModifierGroupDAO dao = new ModifierGroupDAO();
			dao.saveOrUpdate(group);
		} catch (Exception e) {
			MessageDialog.showError(e);
			return false;
		}
		return true;
	}

	@Override
	protected void updateView() {
		MenuModifierGroup group = (MenuModifierGroup) getBean();
		
		if(group.getId() != null && !Hibernate.isInitialized(group.getModifiers())) {
			ModifierDAO dao = new ModifierDAO();
			Session session = dao.getSession();
			group = (MenuModifierGroup) session.merge(group);
			Hibernate.initialize(group.getModifiers());
			session.close();
		}

		tfName.setText(group.getName());
		tfTranslatedName.setText(group.getTranslatedName());
	}

	@Override
	protected boolean updateModel() {
		MenuModifierGroup group = (MenuModifierGroup) getBean();

		String name = tfName.getText();
    	if(POSUtil.isBlankOrNull(name)) {
    		MessageDialog.showError(com.floreantpos.POSConstants.NAME_REQUIRED);
    		return false;
    	}
    	
		group.setName(name);
		group.setTranslatedName(tfTranslatedName.getText());
		
		return true;
	}

	public String getDisplayText() {
    	MenuModifierGroup modifierGroup = (MenuModifierGroup) getBean();
    	if(modifierGroup.getId() == null) {
    		return com.floreantpos.POSConstants.NEW_MODIFIER_GROUP;
    	}
    	return com.floreantpos.POSConstants.EDIT_MODIFIER_GROUP;
    }
}
