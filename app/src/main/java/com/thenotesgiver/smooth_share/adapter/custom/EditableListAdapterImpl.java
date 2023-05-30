package com.thenotesgiver.smooth_share.adapter.custom;

import com.thenotesgiver.smooth_share.framework.util.NotReadyException;

public interface EditableListAdapterImpl<T> extends ListAdapterImpl<T>
{
    T getItem(int position) throws NotReadyException;
}
