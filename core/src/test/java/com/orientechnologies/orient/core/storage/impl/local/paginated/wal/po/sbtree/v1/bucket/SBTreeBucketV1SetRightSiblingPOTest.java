package com.orientechnologies.orient.core.storage.impl.local.paginated.wal.po.sbtree.v1.bucket;

import com.orientechnologies.common.directmemory.OByteBufferPool;
import com.orientechnologies.common.directmemory.OPointer;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.storage.cache.OCacheEntry;
import com.orientechnologies.orient.core.storage.cache.OCacheEntryImpl;
import com.orientechnologies.orient.core.storage.cache.OCachePointer;
import com.orientechnologies.orient.core.storage.impl.local.paginated.wal.OOperationUnitId;
import com.orientechnologies.orient.core.storage.impl.local.paginated.wal.po.PageOperationRecord;
import com.orientechnologies.orient.core.storage.index.sbtree.local.v1.OSBTreeBucketV1;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.List;

public class SBTreeBucketV1SetRightSiblingPOTest {
  @Test
  public void testRedo() {
    final int pageSize = 64 * 1024;
    final OByteBufferPool byteBufferPool = new OByteBufferPool(pageSize);
    try {
      final OPointer pointer = byteBufferPool.acquireDirect(false);
      final OCachePointer cachePointer = new OCachePointer(pointer, byteBufferPool, 0, 0);
      final OCacheEntry entry = new OCacheEntryImpl(0, 0, cachePointer);

      OSBTreeBucketV1<Byte, OIdentifiable> bucket = new OSBTreeBucketV1<>(entry);
      bucket.init(true);

      bucket.setRightSibling(24);

      entry.clearPageOperations();

      final OPointer restoredPointer = byteBufferPool.acquireDirect(false);
      final OCachePointer restoredCachePointer = new OCachePointer(restoredPointer, byteBufferPool, 0, 0);
      final OCacheEntry restoredCacheEntry = new OCacheEntryImpl(0, 0, restoredCachePointer);

      final ByteBuffer originalBuffer = cachePointer.getBufferDuplicate();
      final ByteBuffer restoredBuffer = restoredCachePointer.getBufferDuplicate();

      Assert.assertNotNull(originalBuffer);
      Assert.assertNotNull(restoredBuffer);

      restoredBuffer.put(originalBuffer);

      bucket.setRightSibling(42);

      final List<PageOperationRecord> operations = entry.getPageOperations();
      Assert.assertEquals(1, operations.size());

      Assert.assertTrue(operations.get(0) instanceof SBTreeBucketV1SetRightSiblingPO);

      final SBTreeBucketV1SetRightSiblingPO pageOperation = (SBTreeBucketV1SetRightSiblingPO) operations
          .get(0);

      OSBTreeBucketV1<Byte, OIdentifiable> restoredBucket = new OSBTreeBucketV1<>(restoredCacheEntry);

      Assert.assertEquals(24, restoredBucket.getRightSibling());

      pageOperation.redo(restoredCacheEntry);

      Assert.assertEquals(42, restoredBucket.getRightSibling());

      byteBufferPool.release(pointer);
      byteBufferPool.release(restoredPointer);
    } finally {
      byteBufferPool.clear();
    }
  }

  @Test
  public void testUndo() {
    final int pageSize = 64 * 1024;

    final OByteBufferPool byteBufferPool = new OByteBufferPool(pageSize);
    try {
      final OPointer pointer = byteBufferPool.acquireDirect(false);
      final OCachePointer cachePointer = new OCachePointer(pointer, byteBufferPool, 0, 0);
      final OCacheEntry entry = new OCacheEntryImpl(0, 0, cachePointer);

      OSBTreeBucketV1<Byte, OIdentifiable>  bucket = new OSBTreeBucketV1<>(entry);
      bucket.init(true);

      bucket.setRightSibling(24);

      entry.clearPageOperations();

      bucket.setRightSibling(42);

      final List<PageOperationRecord> operations = entry.getPageOperations();
      Assert.assertEquals(1, operations.size());

      Assert.assertTrue(operations.get(0) instanceof SBTreeBucketV1SetRightSiblingPO);

      final SBTreeBucketV1SetRightSiblingPO pageOperation = (SBTreeBucketV1SetRightSiblingPO) operations
          .get(0);

      final OSBTreeBucketV1<Byte, OIdentifiable>  restoredBucket = new OSBTreeBucketV1<>(entry);

      Assert.assertEquals(42, restoredBucket.getRightSibling());

      pageOperation.undo(entry);

      Assert.assertEquals(24, restoredBucket.getRightSibling());

      byteBufferPool.release(pointer);
    } finally {
      byteBufferPool.clear();
    }
  }

  @Test
  public void testSerialization() {
    OOperationUnitId operationUnitId = OOperationUnitId.generateId();

    SBTreeBucketV1SetRightSiblingPO operation = new SBTreeBucketV1SetRightSiblingPO(42, 24);

    operation.setFileId(42);
    operation.setPageIndex(24);
    operation.setOperationUnitId(operationUnitId);

    final int serializedSize = operation.serializedSize();
    final byte[] stream = new byte[serializedSize + 1];
    int pos = operation.toStream(stream, 1);

    Assert.assertEquals(serializedSize + 1, pos);

    SBTreeBucketV1SetRightSiblingPO restoredOperation = new SBTreeBucketV1SetRightSiblingPO();
    restoredOperation.fromStream(stream, 1);

    Assert.assertEquals(42, restoredOperation.getFileId());
    Assert.assertEquals(24, restoredOperation.getPageIndex());
    Assert.assertEquals(operationUnitId, restoredOperation.getOperationUnitId());

    Assert.assertEquals(42, restoredOperation.getPrevRightSibling());
    Assert.assertEquals(24, restoredOperation.getRightSibling());
  }

}
