package com.example.inventoryapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemHolder> {
    private final List<Item> mItemList; // List of items to display
    private final InventoryDatabase inventoryDatabase; // Database instance for item operations
    private final Context context; // Context for accessing SharedPreferences
    private final Map<MenuItem, String> menuTags = new HashMap<>(); // Map for associating menu items with tags

    public ItemAdapter(List<Item> itemList, Context context, InventoryDatabase inventoryDatabase) {
        this.mItemList = itemList;
        this.inventoryDatabase = inventoryDatabase;
        this.context = context;
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.inventory_item, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        Item mItem = mItemList.get(position);
        holder.bind(mItem);
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    class ItemHolder extends RecyclerView.ViewHolder {
        private final TextView mNameView;
        private final EditText mQuantityView;
        private final ImageButton mDecreaseQuantityBtnInline;
        private final ImageButton mIncreaseQuantityBtnInline;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            mNameView = itemView.findViewById(R.id.itemName);
            mQuantityView = itemView.findViewById(R.id.editQuantity);
            mDecreaseQuantityBtnInline = itemView.findViewById(R.id.decreaseQuantityBtnInline);
            mIncreaseQuantityBtnInline = itemView.findViewById(R.id.increaseQuantityBtnInline);
            ImageButton mItemActionsBtn = itemView.findViewById(R.id.itemActionsBtn);

            mItemActionsBtn.setOnClickListener(this::showPopupMenu);
        }

        public void bind(Item item) {
            mNameView.setText(item.getName());
            mQuantityView.setText(String.valueOf(item.getQuantity()));

            mDecreaseQuantityBtnInline.setOnClickListener(v -> {
                item.decrementQuantity(itemView.getContext());
                boolean updated = inventoryDatabase.updateItem(item);
                if (updated) {
                    mQuantityView.setText(String.valueOf(item.getQuantity()));
                    if (item.getQuantity() == 0 && !areNotificationsEnabled()) {
                        showNotificationDialog();
                    }
                }
            });

            mIncreaseQuantityBtnInline.setOnClickListener(v -> {
                item.incrementQuantity();
                boolean updated = inventoryDatabase.updateItem(item);
                if (updated) {
                    mQuantityView.setText(String.valueOf(item.getQuantity()));
                }
            });
        }

        private boolean areNotificationsEnabled() {
            SharedPreferences preferences = context.getSharedPreferences(SmsNotificationsActivity.PREFS_NAME, Context.MODE_PRIVATE);
            boolean notificationsEnabled = preferences.getBoolean(SmsNotificationsActivity.KEY_RECEIVE_NOTIFICATIONS, false);
            boolean showPromptAgain = preferences.getBoolean(SmsNotificationsActivity.KEY_SHOW_PROMPT_AGAIN, true);
            return notificationsEnabled || !showPromptAgain;
        }

        private void showNotificationDialog() {
            new AlertDialog.Builder(itemView.getContext())
                    .setTitle(R.string.enable_notifications_title)
                    .setMessage(R.string.enable_notifications_message)
                    .setPositiveButton("Yes", (dialog, which) -> {
                        Intent intent = new Intent(itemView.getContext(), SmsNotificationsActivity.class);
                        itemView.getContext().startActivity(intent);
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        SharedPreferences preferences = context.getSharedPreferences(SmsNotificationsActivity.PREFS_NAME, Context.MODE_PRIVATE);
                        preferences.edit().putBoolean(SmsNotificationsActivity.KEY_SHOW_PROMPT_AGAIN, false).apply();
                    })
                    .show();
        }

        private void showPopupMenu(View view) {
            PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.inventory_item_actions_menu, popupMenu.getMenu());

            menuTags.put(popupMenu.getMenu().findItem(R.id.menu_edit), "edit_item");
            menuTags.put(popupMenu.getMenu().findItem(R.id.menu_remove), "remove_item");

            popupMenu.setOnMenuItemClickListener(item -> handleMenuClickByTag(menuTags.get(item), view));
            popupMenu.show();
        }

        private boolean handleMenuClickByTag(String tag, View view) {
            int position = getAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return false;

            Item currentItem = mItemList.get(position);

            switch (tag) {
                case "edit_item":
                    Intent intent = new Intent(view.getContext(), EditItemActivity.class);
                    intent.putExtra(EditItemActivity.EXTRA_ITEM, currentItem);
                    view.getContext().startActivity(intent);
                    return true;

                case "remove_item":
                    new AlertDialog.Builder(view.getContext())
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle(R.string.delete_confirmation_title)
                            .setMessage(R.string.delete_confirmation)
                            .setPositiveButton("Yes", (dialog, which) -> {
                                boolean deleted = inventoryDatabase.deleteItem(currentItem);
                                if (deleted) {
                                    mItemList.remove(position);
                                    notifyItemRemoved(position);
                                } else {
                                    Toast.makeText(view.getContext(), R.string.delete_error, Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                    return true;

                default:
                    return false;
            }
        }
    }
}
