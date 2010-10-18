/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.apps.tag.record;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

import com.android.apps.tag.message.NdefMessageParser;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

import java.util.Arrays;
import java.util.NoSuchElementException;
import javax.annotation.Nullable;

/**
 * A representation of an NFC Forum "Smart Poster".
 */
public class SmartPoster implements ParsedNdefRecord {

    /**
     * NFC Forum Smart Poster Record Type Definition section 3.2.1.
     *
     * "The Title record for the service (there can be many of these in
     * different languages, but a language MUST NOT be repeated).
     * This record is optional."

     */
    private final TextRecord mTitleRecord;

    /**
     * NFC Forum Smart Poster Record Type Definition section 3.2.1.
     *
     * "The URI record. This is the core of the Smart Poster, and all other
     * records are just metadata about this record. There MUST be one URI
     * record and there MUST NOT be more than one."
     */
    private final UriRecord mUriRecord;

    private SmartPoster(UriRecord uri, @Nullable TextRecord title) {
        mUriRecord = Preconditions.checkNotNull(uri);
        mTitleRecord = title;
    }

    public UriRecord getUriRecord() {
        return mUriRecord;
    }

    /**
     * Returns the title of the smart poster.  This may be {@code null}.
     */
    public TextRecord getTitle() {
        return mTitleRecord;
    }

    public static SmartPoster parse(NdefRecord record) {
        Preconditions.checkArgument(record.getTnf() == NdefRecord.TNF_WELL_KNOWN);
        Preconditions.checkArgument(Arrays.equals(record.getType(), NdefRecord.RTD_SMART_POSTER));
        try {
            NdefMessage subRecords = new NdefMessage(record.getPayload());

            Iterable<ParsedNdefRecord> records = NdefMessageParser.getRecords(subRecords);

            UriRecord uri = Iterables.getOnlyElement(Iterables.filter(records, UriRecord.class));
            Iterable<TextRecord> textFields = Iterables.filter(records, TextRecord.class);

            TextRecord title = null;
            if (!Iterables.isEmpty(textFields)) {
                title = Iterables.get(textFields, 0);
            }

            return new SmartPoster(uri, title);
        } catch (FormatException e) {
            throw new IllegalArgumentException(e);
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static boolean isPoster(NdefRecord record) {
        try {
            parse(record);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public String getRecordType() {
        return "SmartPoster";
    }
}