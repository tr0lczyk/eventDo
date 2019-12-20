package com.rad4m.eventdo.models

import com.rad4m.eventdo.utils.Utilities.Companion.ITEM_VIEW_TYPE_HEADER
import com.rad4m.eventdo.utils.Utilities.Companion.ITEM_VIEW_TYPE_ITEM

sealed class DataItem {

    data class DataItemEventModel(val eventModel: EventModel) : DataItem() {
        override val viewType: Int
            get() = ITEM_VIEW_TYPE_ITEM
    }

    data class DataItemHeader(val date: String) : DataItem() {
        override val viewType: Int
            get() = ITEM_VIEW_TYPE_HEADER
    }

    abstract val viewType: Int
}