package br.com.bea.androidtools.test.json;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import br.com.bea.androidtools.api.json.JSONContext;
import br.com.bea.androidtools.api.json.JSONContextImpl;
import br.com.bea.androidtools.test.model.SimpleEntity;

public class JSONContextTest {

    private static final byte[] DATA = new byte[] { 0x00 };
    private static final long ID = 1L;
    private static final String NAME = "mock";
    private static final BigDecimal PRICE = new BigDecimal("0.00");
    private static final Date VERSION = new Date();

    private final JSONContext<SimpleEntity> context = new JSONContextImpl<SimpleEntity>(SimpleEntity.class);
    private final JSONArray mockArray = new JSONArray();
    private final JSONObject mockObject = new JSONObject();
    private final SimpleEntity mockSimpleEntity = new SimpleEntity();
    private final JSONObject mockWrongObject = new JSONObject();

    @Before
    public void setUp() throws Exception {
        mockSimpleEntity.setId(ID);
        mockSimpleEntity.setName(NAME);
        mockSimpleEntity.setPrice(PRICE);
        mockSimpleEntity.setVersion(VERSION);
        mockSimpleEntity.setData(DATA);
        mockObject.put("id", ID);
        mockObject.put("name", NAME);
        mockObject.put("price", PRICE);
        mockObject.put("version", VERSION);
        mockObject.put("data", DATA);
        mockArray.put(mockObject);
        mockWrongObject.put("id", ID);
        mockWrongObject.put("name", "test");
    }

    @Test
    public void testMarshal() {
        final JSONArray array = context.marshal(Arrays.asList(mockSimpleEntity));
        Assert.assertNotNull(array);
        Assert.assertNotSame(array, mockArray);
        Assert.assertEquals(array.toString(), mockArray.toString());
    }

    @Test
    public void testSingle() {
        final JSONObject object = context.single(mockSimpleEntity);
        Assert.assertNotNull(object);
        Assert.assertNotSame(object, mockObject);
        Assert.assertEquals(object.toString(), mockObject.toString());
    }

    @Test
    public void testUnmarshal() {
        final List<SimpleEntity> list = context.unmarshal(mockArray);
        Assert.assertNotEquals(list.size(), 0);
        Assert.assertNotNull(list);
        Assert.assertNotSame(list, Arrays.asList(mockSimpleEntity));
        Assert.assertArrayEquals(list.toArray(), Arrays.asList(mockSimpleEntity).toArray());
    }

    @Test
    public void testWrongEntity() {
        final JSONObject object = context.single(mockSimpleEntity);
        Assert.assertNotNull(object);
        Assert.assertNotSame(object, mockWrongObject);
        Assert.assertNotEquals(object.toString(), mockWrongObject.toString());
    }

}
