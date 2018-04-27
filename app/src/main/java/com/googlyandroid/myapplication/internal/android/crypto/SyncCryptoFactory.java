/*
 * Copyright 2016 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.googlyandroid.myapplication.internal.android.crypto;

import android.content.Context;
import android.os.Build;

import com.googlyandroid.myapplication.internal.android.crypto.api_18.SyncCryptoApi18Impl;
import com.googlyandroid.myapplication.internal.android.crypto.api_23.SyncCryptoApi23Impl;
import com.googlyandroid.myapplication.internal.android.crypto.api_legacy.SyncCryptoLegacy;
import java.security.KeyStoreException;


/**
 */
public class SyncCryptoFactory {

    private static final boolean IS_API_23 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    private static final boolean IS_API_18 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    private static final boolean IS_API_LEGACY = Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;

    public static SyncCrypto get (Context context) throws KeyStoreException {
        if (IS_API_23) {
            return new SyncCryptoApi23Impl(context);
        } else if (IS_API_18) {
            return new SyncCryptoApi18Impl(context);
        } else if (IS_API_LEGACY) {
            return new SyncCryptoLegacy(context);
        } else {
            throw new KeyStoreException("Unknown android version");
        }
    }
}
