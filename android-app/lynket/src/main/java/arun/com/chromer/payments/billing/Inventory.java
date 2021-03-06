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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a block of information about in-app items.
 * An Inventory is returned by such methods as {@link IabHelper#queryInventory}.
 */
@SuppressWarnings("ALL")
public class Inventory {
  private Map<String, SkuDetails> mSkuMap = new HashMap<String, SkuDetails>();
  private Map<String, Purchase> mPurchaseMap = new HashMap<String, Purchase>();

  Inventory() {
  }

  /**
   * Returns the listing details for an in-app product.
   */
  public SkuDetails getSkuDetails(String sku) {
    return mSkuMap.get(sku);
  }

  /**
   * Returns purchase information for a given product, or null if there is no purchase.
   */
  public Purchase getPurchase(String sku) {
    return mPurchaseMap.get(sku);
  }

  /**
   * Returns whether or not there exists a purchase of the given product.
   */
  public boolean hasPurchase(String sku) {
    return mPurchaseMap.containsKey(sku);
  }

  /**
   * Return whether or not details about the given product are available.
   */
  public boolean hasDetails(String sku) {
    return mSkuMap.containsKey(sku);
  }

  /**
   * Erase a purchase (locally) from the inventory, given its product ID. This just
   * modifies the Inventory object locally and has no effect on the server! This is
   * useful when you have an existing Inventory object which you know to be up to date,
   * and you have just consumed an item successfully, which means that erasing its
   * purchase data from the Inventory you already have is quicker than querying for
   * a new Inventory.
   */
  public void erasePurchase(String sku) {
    if (mPurchaseMap.containsKey(sku)) mPurchaseMap.remove(sku);
  }

  /**
   * Returns a list of all owned product IDs.
   */
  List<String> getAllOwnedSkus() {
    return new ArrayList<String>(mPurchaseMap.keySet());
  }

  /**
   * Returns a list of all owned product IDs of a given type
   */
  List<String> getAllOwnedSkus(String itemType) {
    List<String> result = new ArrayList<String>();
    for (Purchase p : mPurchaseMap.values()) {
      if (p.getItemType().equals(itemType)) result.add(p.getSku());
    }
    return result;
  }

  /**
   * Returns a list of all purchases.
   */
  List<Purchase> getAllPurchases() {
    return new ArrayList<Purchase>(mPurchaseMap.values());
  }

  void addSkuDetails(SkuDetails d) {
    mSkuMap.put(d.getSku(), d);
  }

  void addPurchase(Purchase p) {
    mPurchaseMap.put(p.getSku(), p);
  }
}
