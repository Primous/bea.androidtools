package br.com.bea.androidtools.api.android;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public final class AndroidUtils {

    public static final boolean hasConnectivity(final Context context, final int... networkTypes) {
        final ConnectivityManager connectivity = (ConnectivityManager) context
            .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (networkTypes.length > 0)
            for (final int networkType : networkTypes)
                if (ConnectivityManager.isNetworkTypeValid(networkType))
                    return hasConnectivity(connectivity.getNetworkInfo(networkType));
        return hasConnectivity(connectivity.getActiveNetworkInfo());
    }

    private static boolean hasConnectivity(final NetworkInfo info) {
        return null != info && info.isAvailable() && info.isConnected();
    }

    public static final boolean isConnected(final Context context, final int... networkTypes) {
        final ConnectivityManager connectivity = (ConnectivityManager) context
            .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (networkTypes.length > 0)
            for (final int networkType : networkTypes)
                if (ConnectivityManager.isNetworkTypeValid(networkType))
                    return isConnected(connectivity.getNetworkInfo(networkType));
        return isConnected(connectivity.getActiveNetworkInfo());
    }

    private static boolean isConnected(final NetworkInfo info) {
        return null != info && info.isConnected();
    }
}
