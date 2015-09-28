package com.aluvi.android.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aluvi.android.R;
import com.aluvi.android.api.users.models.ReceiptData;
import com.aluvi.android.fragments.base.BaseButterFragment;
import com.aluvi.android.helpers.CurrencyUtils;
import com.aluvi.android.helpers.views.DividerItemDecoration;
import com.aluvi.android.managers.PaymentManager;
import com.aluvi.android.managers.UserStateManager;
import com.aluvi.android.managers.callbacks.Callback;
import com.aluvi.android.managers.callbacks.DataCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by usama on 8/20/15.
 */
public class ReceiptsFragment extends BaseButterFragment {
    @Bind(R.id.receipts_recycler_view) RecyclerView mReceiptsRecyclerView;

    private ReceiptsAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public static ReceiptsFragment newInstance() {
        return new ReceiptsFragment();
    }

    @Override
    public View getRootView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_receipts, container, false);
    }

    @Override
    public void initUI() {
        mLayoutManager = new LinearLayoutManager(getActivity());
        mAdapter = new ReceiptsAdapter(new ArrayList<ReceiptData>());

        mReceiptsRecyclerView.setLayoutManager(mLayoutManager);
        mReceiptsRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), android.R.color.white));
        mReceiptsRecyclerView.setAdapter(mAdapter);
        fetchReceipts();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.receipts_button_print_email)
    public void onPrintReceiptsButtonClicked() {
        showDefaultProgressDialog();
        UserStateManager.getInstance().emailReceipts(new Callback() {
            @Override
            public void success() {
                cancelProgressDialogs();
                if (getView() != null)
                    Snackbar.make(getView(), R.string.receipts_emailed, Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void failure(String message) {
                cancelProgressDialogs();
                if (getView() != null)
                    Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchReceipts() {
        showDefaultProgressDialog();
        PaymentManager.getInstance().getReceipts(new DataCallback<List<ReceiptData>>() {
            @Override
            public void success(List<ReceiptData> result) {
                cancelProgressDialogs();

                if (mReceiptsRecyclerView != null) {
                    mAdapter.getData().clear();
                    mAdapter.getData().addAll(result);
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void failure(String message) {
                cancelProgressDialogs();
            }
        });
    }

    public static class ReceiptsAdapter extends RecyclerView.Adapter<ReceiptsAdapter.ViewHolder> {
        public static class ViewHolder extends RecyclerView.ViewHolder {
            @Bind(R.id.receipt_text_view_date) TextView mReceiptDateTextView;
            @Bind(R.id.receipt_text_view_type) TextView mReceiptTypeTextView;
            @Bind(R.id.receipt_text_view_price) TextView mReceiptPriceTextView;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                mReceiptTypeTextView = (TextView) itemView.findViewById(R.id.receipt_text_view_type);
                mReceiptPriceTextView = (TextView) itemView.findViewById(R.id.receipt_text_view_price);
            }
        }

        private List<ReceiptData> mReceipts;

        public ReceiptsAdapter(List<ReceiptData> receipts) {
            mReceipts = receipts;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View rowLayout = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_layout_receipt, parent, false);
            return new ViewHolder(rowLayout);
        }

        private SimpleDateFormat monthDayFormat = new SimpleDateFormat("M/dd");

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            ReceiptData receipt = mReceipts.get(position);
            viewHolder.mReceiptDateTextView.setText(monthDayFormat.format(receipt.getDate()));
            viewHolder.mReceiptPriceTextView.setText(CurrencyUtils.getFormattedDollars(receipt.getAmount()));
            viewHolder.mReceiptTypeTextView.setText(receipt.getType());
        }

        @Override
        public int getItemCount() {
            return mReceipts.size();
        }

        public List<ReceiptData> getData() {
            return mReceipts;
        }
    }
}
