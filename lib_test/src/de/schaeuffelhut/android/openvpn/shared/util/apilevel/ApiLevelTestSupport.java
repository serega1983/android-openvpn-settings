package de.schaeuffelhut.android.openvpn.shared.util.apilevel;

import static org.mockito.Mockito.mock;

/**
 * @author Friedrich Schäuffelhut
 * @since 2013-04-02
 */
public class ApiLevelTestSupport
{
    public static ApiLevel mockApiLevel()
    {
        ApiLevel apiLevelMock = mock( ApiLevel.class );
        ApiLevel.set( apiLevelMock );
        return apiLevelMock;
    }
}
