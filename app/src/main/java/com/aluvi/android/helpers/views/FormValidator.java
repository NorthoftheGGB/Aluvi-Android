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
        return addField(editText, mErrorMessage, validator);
    }

    public FormValidator addField(EditText editText, String error, Validator validator) {
        mFieldsToValidate.add(new Validation(editText, error, validator));
        return this;
    }

    public boolean validate() {
        boolean isErrorFree = true;
        for (Validation validation : mFieldsToValidate) {
            validation.getField().setError(null);
            if (!validation.isInputValid()) {
                String error = validation.getError() != null ? validation.getError() : mErrorMessage;
                validation.getField().setError(error);
                isErrorFree = false;
            }
        }

        return isErrorFree;
    }

    private static class Validation {
        private EditText mField;
        private String mError;
        private Validator mValidator;

        public Validation(EditText mField, String error, Validator mValidator) {
            this.mField = mField;
            this.mValidator = mValidator;
            this.mError = error;
        }

        public String getError() {
            return mError;
        }

        public void setError(String mError) {
            this.mError = mError;
        }

        public boolean isInputValid() {
            return mValidator.isValid(mField.getText().toString());
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
