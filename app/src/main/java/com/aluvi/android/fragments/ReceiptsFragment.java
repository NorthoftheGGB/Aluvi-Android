package com.aluvi.android.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.aluvi.android.managers.PaymentManager;
import com.aluvi.android.managers.callbacks.DataCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

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
        mReceiptsRecyclerView.setAdapter(mAdapter);
        mReceiptsRecyclerView.setHasFixedSize(true);
        fetchReceipts();
    }

    private void fetchReceipts() {
        PaymentManager.getInstance().getReceipts(new DataCallback<List<ReceiptData>>() {
            @Override
            public void success(List<ReceiptData> result) {
                if (mReceiptsRecyclerView != null) {
                    mAdapter.getData().clear();
                    mAdapter.getData().addAll(result);
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void failure(String message) {
            }
        });
    }

    private static class ReceiptsAdapter extends RecyclerView.Adapter<ReceiptsAdapter.ViewHolder> {
        public static class ViewHolder extends RecyclerView.ViewHolder {
            public TextView mReceiptTypeTextView, mReceiptPriceTextView;

            public ViewHolder(View itemView) {
                super(itemView);
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

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            ReceiptData receipt = mReceipts.get(position);
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
