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
package com.floreantpos.ui.dialog;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import com.floreantpos.POSConstants;
import com.floreantpos.main.Application;
import com.floreantpos.model.CashDrawer;
import com.floreantpos.model.Currency;
import com.floreantpos.model.CurrencyBalance;
import com.floreantpos.model.Terminal;
import com.floreantpos.model.dao.CashDrawerDAO;
import com.floreantpos.swing.DoubleTextField;
import com.floreantpos.swing.NumericKeypad;
import com.floreantpos.swing.PosUIManager;
import com.floreantpos.util.NumberUtil;

public class MultiCurrencyTenderDialog extends OkCancelOptionDialog implements FocusListener {
	private List<Currency> currencyList;
	private double dueAmount;
	private double totalTenderedAmount;

	private List<CurrencyRow> currencyRows = new ArrayList();
	private Map<Integer, CurrencyBalance> currencyBalanceMap = new HashMap<Integer, CurrencyBalance>();
	private CashDrawer cashDrawer;

	public MultiCurrencyTenderDialog(double dueAmount, List<Currency> currencyList) {
		super();
		this.currencyList = currencyList;
		this.dueAmount = dueAmount;
		init();
	}

	private void init() {
		JPanel contentPane = getContentPanel();
		setOkButtonText(POSConstants.SAVE_BUTTON_TEXT);
		setTitle("Enter tendered amount");
		setTitlePaneText("Enter tendered amount");

		MigLayout layout = new MigLayout("inset 0", "[grow,fill]", "[grow,fill]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		contentPane.setLayout(layout);

		JPanel inputPanel = new JPanel();
		GridLayout gridLayout = new GridLayout(0, 3, 5, 5);
		inputPanel.setLayout(gridLayout);

		JLabel lblRemainingAmount = getJLabel("Remaining Amount", Font.BOLD, 16, JLabel.CENTER);
		JLabel lblCurrency = getJLabel("Currency", Font.BOLD, 16, JLabel.CENTER);
		JLabel lblTendered = getJLabel("Tendered", Font.BOLD, 16, JLabel.CENTER);

		inputPanel.add(lblRemainingAmount);
		inputPanel.add(lblCurrency);
		inputPanel.add(lblTendered);

		for (Currency currency : currencyList) {
			String dueAmountByCurrency = NumberUtil.formatNumber(currency.getExchangeRate() * dueAmount);
			JLabel lblRemainingBalance = getJLabel(dueAmountByCurrency, Font.PLAIN, 16, JLabel.LEFT);
			JLabel currencyName = getJLabel(currency.getName(), Font.PLAIN, 16, JLabel.LEFT);
			DoubleTextField tfTenderedAmount = getDoubleTextField("", Font.PLAIN, 16, JTextField.RIGHT);

			inputPanel.add(lblRemainingBalance);
			inputPanel.add(currencyName);
			inputPanel.add(tfTenderedAmount);

			tfTenderedAmount.addFocusListener(this);

			CurrencyRow item = new CurrencyRow(currency, lblRemainingBalance, tfTenderedAmount);
			currencyRows.add(item);
		}
		contentPane.add(inputPanel, "cell 0 0,alignx left,aligny top"); //$NON-NLS-1$

		NumericKeypad numericKeypad = new NumericKeypad();
		contentPane.add(numericKeypad, "cell 0 1"); //$NON-NLS-1$
	}

	private JLabel getJLabel(String text, int bold, int fontSize, int align) {
		JLabel lbl = new JLabel(text);
		lbl.setFont(lbl.getFont().deriveFont(bold, PosUIManager.getSize(fontSize)));
		lbl.setHorizontalAlignment(align);
		return lbl;
	}

	private DoubleTextField getDoubleTextField(String text, int bold, int fontSize, int align) {
		DoubleTextField tf = new DoubleTextField();
		tf.setText(text);
		tf.setFont(tf.getFont().deriveFont(bold, PosUIManager.getSize(fontSize)));
		tf.setHorizontalAlignment(align);
		tf.setBackground(Color.WHITE);
		return tf;
	}

	private class CurrencyRow {
		Currency currency;
		JLabel lblRemainingBalance;
		DoubleTextField tfTenderdAmount;
		double creditAmount = 0;
		double cashBackAmount = 0;
		double tenderAmount = 0;

		public CurrencyRow(Currency currency, JLabel lblRemainingBalance, DoubleTextField tfTenderedAmount) {
			this.currency = currency;
			this.lblRemainingBalance = lblRemainingBalance;
			this.tfTenderdAmount = tfTenderedAmount;
		}

		void setCashBackAmount(double cashBackAmount) {
			this.cashBackAmount = cashBackAmount;
		}

		void setTenderAmount(double tenderAmount) {
			this.tenderAmount = tenderAmount;
		}

		void setCreditAmount(double creditAmount) {
			this.creditAmount = creditAmount;
		}
	}

	@Override
	public void doOk() {
		Terminal terminal = Application.getInstance().getTerminal();

		cashDrawer = CashDrawerDAO.getInstance().findByTerminal(terminal);
		if (cashDrawer == null) {
			cashDrawer = new CashDrawer();
			cashDrawer.setTerminal(terminal);

			if (cashDrawer.getCurrencyBalanceList() == null) {
				cashDrawer.setCurrencyBalanceList(new HashSet());
			}
		}
		for (CurrencyBalance currencyBalance : cashDrawer.getCurrencyBalanceList()) {
			currencyBalanceMap.put(currencyBalance.getCurrency().getId(), currencyBalance);
		}

		for (CurrencyRow rowItem : currencyRows) {
			CurrencyBalance item = currencyBalanceMap.get(rowItem.currency.getId());
			if (item == null) {
				item = new CurrencyBalance();
				item.setCurrency(rowItem.currency);
				item.setCashDrawer(cashDrawer);
				currencyBalanceMap.put(rowItem.currency.getId(), item);
				cashDrawer.addTocurrencyBalanceList(item);
			}
			double tenderAmount = rowItem.tenderAmount;
			double cashBackAmount = rowItem.cashBackAmount;
			item.setCashCreditAmount(rowItem.creditAmount);
			item.setCashBackAmount(cashBackAmount);
			item.setBalance(item.getBalance() + item.getCashCreditAmount());
		}
		setCanceled(false);
		dispose();
	}

	public Map<Integer, CurrencyBalance> getCurrencyBalanceMap() {
		return currencyBalanceMap;
	}

	public CashDrawer getCashDrawer() {
		return cashDrawer;
	}

	@Override
	public void focusGained(FocusEvent e) {
	}

	@Override
	public void focusLost(FocusEvent e) {
		totalTenderedAmount = 0;
		for (CurrencyRow rowItem : currencyRows) {
			double value = rowItem.tfTenderdAmount.getDouble();
			if (Double.isNaN(value)) {
				value = 0.0;
			}
			rowItem.setTenderAmount(value);
			totalTenderedAmount += (value / rowItem.currency.getExchangeRate());
			if (totalTenderedAmount <= dueAmount) {
				rowItem.setCreditAmount(value);
			}
			else {
				double remainingBalance = (dueAmount - totalTenderedAmount) * rowItem.currency.getExchangeRate();
				remainingBalance = Math.abs(remainingBalance);
				rowItem.setCreditAmount(value - remainingBalance);
				rowItem.setCashBackAmount(remainingBalance);
				break;
			}
		}
		for (CurrencyRow currInput : currencyRows) {
			double remainingBalance = (dueAmount - totalTenderedAmount) * currInput.currency.getExchangeRate();
			currInput.lblRemainingBalance.setText(NumberUtil.formatNumber(remainingBalance));
		}
	}

	public boolean hasCashBack() {
		if (dueAmount < totalTenderedAmount) {
			return true;
		}
		return false;
	}

	public double getTenderedAmount() {
		return totalTenderedAmount;
	}

	public double getChangeDueAmount() {
		return totalTenderedAmount - dueAmount;
	}
}
