package com.aisleshare;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.hudomju.swipe.SwipeToDismissTouchListener;
import com.hudomju.swipe.adapter.ListViewAdapter;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;


public class CurrentList extends AppCompatActivity {

    // Class Variables
    private ListView listView;
    private ArrayList<Item> items;
    private CustomAdapter customAdapter;
    private boolean isIncreasingOrder;
    private int currentOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_list);

        listView = (ListView)findViewById(R.id.currentItems);
        items = new ArrayList<>();
        isIncreasingOrder = true;
        currentOrder = -1;
        customAdapter = new CustomAdapter(this, items);
        listView.setAdapter(customAdapter);

        setupTestItems();
        setListTitle(savedInstanceState);
        addButtonListener();
        setSwipeAdapter();
    }

    // Sorted based on the order index parameter
    public void sortList(boolean reverseOrder, int order) {
        if(reverseOrder) {
            isIncreasingOrder = !isIncreasingOrder;
        }
        if(order != currentOrder){
            currentOrder = order;
            isIncreasingOrder = true;
        }

        ItemComparator compare = new ItemComparator();

        // Unsorted
        if(currentOrder == -1){
            return;
        }
        // Name
        else if(currentOrder == 0){
            ItemComparator.Name sorter = compare.new Name();
            Collections.sort(items, sorter);
        }
        // Quantity
        else if(currentOrder == 1){
            ItemComparator.Quantity sorter = compare.new Quantity();
            Collections.sort(items, sorter);
        }
        // Time Created
        else if(currentOrder == 2){
            ItemComparator.Created sorter = compare.new Created();
            Collections.sort(items, sorter);
        }
        // Type
        else if(currentOrder == 3){
            ItemComparator.Type sorter = compare.new Type();
            Collections.sort(items, sorter);
        }

        if(!isIncreasingOrder){
            Collections.reverse(items);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_current_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem option) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = option.getItemId();

        if(id == R.id.sort){
            return super.onOptionsItemSelected(option);
        }

        //noinspection SimplifiableIfStatement
        switch(id) {
            case R.id.sort_name:
                sortList(true, 0);
                break;
            case R.id.sort_quantity:
                sortList(true, 1);
                break;
            case R.id.sort_time:
                sortList(true, 2);
                break;
            case R.id.sort_type:
                sortList(true, 3);
                break;
        }

        customAdapter.notifyDataSetChanged();
        return super.onOptionsItemSelected(option);
    }

    // Popup for adding an Item
    public void addItemDialog(){
        // custom dialog
        final Dialog dialog = new Dialog(CurrentList.this);
        dialog.setContentView(R.layout.add_item_dialog);
        dialog.setTitle("Add a New Item");

        final EditText itemName = (EditText) dialog.findViewById(R.id.Name);
        final EditText itemType = (EditText) dialog.findViewById(R.id.Type);
        final Button minus = (Button) dialog.findViewById(R.id.Minus);
        final EditText itemQuantity = (EditText) dialog.findViewById(R.id.Quantity);
        final EditText itemUnits = (EditText) dialog.findViewById(R.id.units);
        final Button plus = (Button) dialog.findViewById(R.id.Plus);
        final Button cancel = (Button) dialog.findViewById(R.id.Cancel);
        final Button more = (Button) dialog.findViewById(R.id.More);
        final Button done = (Button) dialog.findViewById(R.id.Done);

        // Open keyboard automatically
        itemName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        // Notify user about duplicate item
        itemName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String name = itemName.getText().toString();
                for(int index = 0; index < items.size(); index++){
                    if(name.toLowerCase().equals(items.get(index).getName().toLowerCase())){
                        Context context = getApplicationContext();
                        CharSequence text = "Is this a Duplicate?";
                        int duration = Toast.LENGTH_LONG;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.setGravity(Gravity.TOP, 0, 30);
                        toast.show();
                    }
                }

            }
        });

        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!itemQuantity.getText().toString().isEmpty()){
                    double value = Double.parseDouble(itemQuantity.getText().toString());
                    if (value > 1) {
                        itemQuantity.setText(String.format("%s", (int) Math.ceil(value - 1)));
                    }
                }
            }
        });

        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!itemQuantity.getText().toString().isEmpty()) {
                    double value = Double.parseDouble(itemQuantity.getText().toString());
                    itemQuantity.setText(String.format("%s", (int)Math.floor(value + 1)));
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!itemName.getText().toString().isEmpty()) {
                    String name = itemName.getText().toString();
                    String type = itemType.getText().toString();
                    double quantity;
                    String units = itemUnits.getText().toString();
                    if(!itemQuantity.getText().toString().isEmpty()) {
                        quantity = Double.parseDouble(itemQuantity.getText().toString());
                    }
                    else{
                        quantity = 1;
                    }
                    Item m = new Item(name, type, quantity, units);
                    items.add(m);
                    sortList(false, currentOrder);
                    customAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                    addItemDialog();
                }
                else{
                    itemName.setError("Name is empty...");
                }
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!itemName.getText().toString().isEmpty()) {
                    String name = itemName.getText().toString();
                    String type = itemType.getText().toString();
                    double quantity;
                    String units = itemUnits.getText().toString();
                    if(!itemQuantity.getText().toString().isEmpty()) {
                        quantity = Double.parseDouble(itemQuantity.getText().toString());
                    }
                    else{
                        quantity = 1;
                    }
                    Item m = new Item(name, type, quantity, units);
                    items.add(m);
                    sortList(false, currentOrder);
                    customAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                }
                else{
                    itemName.setError("Name is empty...");
                }
            }
        });

        dialog.show();
    }

    // Checks/UnChecks an item by clicking on any element in its row
    public void itemClick(View v){
        Item item = items.get(v.getId());
        if (item.getChecked() == 0){
            item.setChecked(1);
        }
        else{
            item.setChecked(0);
        }
        sortList(false, currentOrder);
        customAdapter.notifyDataSetChanged();
    }

    public void rowClick(int position){
        Item item = items.get(position);
        if (item.getChecked() == 0){
            item.setChecked(1);
        }
        else{
            item.setChecked(0);
        }
        sortList(false, currentOrder);
        customAdapter.notifyDataSetChanged();
    }

    public void setSwipeAdapter(){
        // TODO: Fix issue with swiping multiple items concurrently
        final SwipeToDismissTouchListener<ListViewAdapter> touchListener =
                new SwipeToDismissTouchListener<>(
                        new ListViewAdapter(listView),
                        new SwipeToDismissTouchListener.DismissCallbacks<ListViewAdapter>() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(ListViewAdapter view, int position) {
                                items.remove(position);
                                customAdapter.notifyDataSetChanged();
                            }
                        });
        listView.setOnTouchListener(touchListener);
        listView.setOnScrollListener((AbsListView.OnScrollListener) touchListener.makeScrollListener());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (touchListener.existPendingDismisses()) {
                    touchListener.undoPendingDismiss();
                } else {
                    rowClick(position);
                }
            }
        });
    }

    public void setupTestItems(){
        ArrayList<String> jsonList = new ArrayList<>();
        jsonList.add("{\"name\":itemName,\"quantity\":1,\"units\":unit,\"type\":defType, \"timeCreated\":12105543, \"checked\":0}");
        jsonList.add("{\"name\":burgers,\"quantity\":5,\"units\":\"\",\"type\":Meats, \"timeCreated\":12105543, \"checked\":0}");
        jsonList.add("{\"name\":Eggs,\"quantity\":2,\"units\":dozen,\"type\":\"\", \"timeCreated\":12104543, \"checked\":0}");
        jsonList.add("{\"name\":Bacon,\"quantity\":100,\"units\":strips,\"type\":Meats, \"timeCreated\":12105533, \"checked\":0}");
        jsonList.add("{\"name\":Cheese,\"quantity\":4,\"units\":slices,\"type\":Dairy, \"timeCreated\":13105543, \"checked\":0}");
        jsonList.add("{\"name\":Buns,\"quantity\":1,\"units\":\"\",\"type\":\"\", \"timeCreated\":12105843, \"checked\":0}");

        JSONObject obj;
        for(int i = 0; i < jsonList.size(); i++){
            try {
                obj = new JSONObject(jsonList.get(i));
                items.add(new Item(obj.getString("name"), obj.getString("type"), obj.getInt("quantity"),
                        obj.getString("units"), obj.getInt("timeCreated"), obj.getInt("checked")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void setListTitle(Bundle savedInstanceState){
        String listTitle;
        if (savedInstanceState == null) {
            listTitle = getIntent().getStringExtra("com.ShoppingList.MESSAGE");
        }
        else {
            listTitle = (String) savedInstanceState.getSerializable("com.ShoppingList.MESSAGE");
        }
        setTitle(listTitle);
    }

    public void addButtonListener(){
        FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.float_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItemDialog();
            }
        });
    }
}
