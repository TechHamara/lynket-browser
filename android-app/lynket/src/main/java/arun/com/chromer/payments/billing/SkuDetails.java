/*
 *
 *  Lynket
 *
 *  Copyright (C) 2022 Arunkumar
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.payments.billing;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents an in-app product's listing details.
 */
@SuppressWarnings("FieldCanBeLocal")
public class SkuDetails {
  private final String mItemType;
  private final String mSku;
  private final String mType;
  private final String mPrice;
  private final long mPriceAmountMicros;
  private final String mPriceCurrencyCode;
  private final String mTitle;
  private final String mDescription;
  private final String mJson;

  public SkuDetails(String jsonSkuDetails) throws JSONException {
    this(IabHelper.ITEM_TYPE_INAPP, jsonSkuDetails);
  }

  public SkuDetails(String itemType, String jsonSkuDetails) throws JSONException {
    mItemType = itemType;
    mJson = jsonSkuDetails;
    JSONObject o = new JSONObject(mJson);
    mSku = o.optString("productId");
    mType = o.optString("type");
    mPrice = o.optString("price");
    mPriceAmountMicros = o.optLong("price_amount_micros");
    mPriceCurrencyCode = o.optString("price_currency_code");
    mTitle = o.optString("title");
    mDescription = o.optString("description");
  }

  public String getSku() {
    return mSku;
  }

  public String getType() {
    return mType;
  }

  public String getPrice() {
    return mPrice;
  }

  public long getPriceAmountMicros() {
    return mPriceAmountMicros;
  }

  public String getPriceCurrencyCode() {
    return mPriceCurrencyCode;
  }

  public String getTitle() {
    return mTitle;
  }

  public String getDescription() {
    return mDescription;
  }

  @Override
  public String toString() {
    return "SkuDetails:" + mJson;
  }
}
