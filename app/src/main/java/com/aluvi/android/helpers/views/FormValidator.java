package com.aluvi.android.helpers.views;

import android.widget.EditText;

import java.util.ArrayList;

/**
 * Created by usama on 8/8/15.
 */
public class FormValidator {
    private String mErrorMessage;
    private ArrayList<Validation> mFieldsToValidate = new ArrayList<>();

    public FormValidator(String errorMessage) {
        mErrorMessage = errorMessage;
    }

    public FormValidator addField(EditText editText) {
        return addField(editText, new Validator() {
            @Override
            public boolean isValid(String input) {
                return !"".equals(input);
            }
        });
    }

    public FormValidator addField(EditText editText, Validator validator) {
        mFieldsToValidate.add(new Validation(editText, validator));
        return this;
    }

    public boolean validate() {
        boolean isErrorFree = true;
        for (Validation validation : mFieldsToValidate) {
            if (!validation.isInputValid()) {
                validation.getField().setError(mErrorMessage);
                isErrorFree = false;
            }
        }

        return isErrorFree;
    }

    private static class Validation {
        private EditText mField;
        private Validator mValidator;

        public boolean isInputValid() {
            return mValidator.isValid(mField.getText().toString());
        }

        public Validation(EditText mField, Validator mValidator) {
            this.mField = mField;
            this.mValidator = mValidator;
        }

        public EditText getField() {
            return mField;
        }

        public void setField(EditText mField) {
            this.mField = mField;
        }

        public Validator getValidator() {
            return mValidator;
        }

        public void setValidator(Validator mValidator) {
            this.mValidator = mValidator;
        }
    }

    public interface Validator {
        boolean isValid(String input);
    }
}
