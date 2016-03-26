package com.json.post.rg;

import android.app.Activity;
import android.os.AsyncTask;

/**
 * Created by Miki on 17/03/2016.
 */
public class Product extends AsyncTask<String,String,String> {
    private String name;
    private String price;
    private String description;

    public Product(String name, String price, String description){
        this.name=name;
        this.price=price;
        this.description=description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    @Override
    protected String doInBackground(String... params) {
        return null;
    }


}
