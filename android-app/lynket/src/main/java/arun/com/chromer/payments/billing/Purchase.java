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
 * Represents an in-app billing purchase.
 */
@SuppressWarnings("ALL")
public class Purchase {
  String mItemType;  // ITEM_TYPE_INAPP or ITEM_TYPE_SUBS
  String mOrderId;
  String mPackageName;
  String mSku;
  long mPurchaseTime;
  int mPurchaseState;
  String mDeveloperPayload;
  String mToken;
  String mOriginalJson;
  String mSignature;
  boolean mIsAutoRenewing;

  public Purchase(String itemType, String jsonPurchaseInfo, String signature) throws JSONException {
    mItemType = itemType;
    mOriginalJson = jsonPurchaseInfo;
    JSONObject o = new JSONObject(mOriginalJson);
    mOrderId = o.optString("orderId");
    mPackageName = o.optString("packageName");
    mSku = o.optString("productId");
    mPurchaseTime = o.optLong("purchaseTime");
    mPurchaseState = o.optInt("purchaseState");
    mDeveloperPayload = o.optString("developerPayload");
    mToken = o.optString("token", o.optString("purchaseToken"));
    mIsAutoRenewing = o.optBoolean("autoRenewing");
    mSignature = signature;
  }

  public String getItemType() {
    return mItemType;
  }

  public String getOrderId() {
    return mOrderId;
  }

  public String getPackageName() {
    return mPackageName;
  }

  public String getSku() {
    return mSku;
  }

  public long getPurchaseTime() {
    return mPurchaseTime;
  }

  public int getPurchaseState() {
    return mPurchaseState;
  }

  public String getDeveloperPayload() {
    return mDeveloperPayload;
  }

  public String getToken() {
    return mToken;
  }

  public String getOriginalJson() {
    return mOriginalJson;
  }

  public String getSignature() {
    return mSignature;
  }

  public boolean isAutoRenewing() {
    return mIsAutoRenewing;
  }

  @Override
  public String toString() {
    return "PurchaseInfo(type:" + mItemType + "):" + mOriginalJson;
  }
}
