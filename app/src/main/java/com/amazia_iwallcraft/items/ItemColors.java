package com.amazia_iwallcraft.items;

import com.google.gson.annotations.SerializedName;

public class ItemColors {

	@SerializedName("color_id")
	String id;
	@SerializedName("color_name")
	String colorName;
	@SerializedName("color_code")
	String colorHex;

	public ItemColors(String id, String colorName, String colorHex) {
		this.id = id;
		this.colorName = colorName;
		this.colorHex = colorHex;
	}

	public String getId() {
		return id;
	}

	public String getColorName() {
		return colorName;
	}

	public String getColorHex() {
		return colorHex;
	}
}