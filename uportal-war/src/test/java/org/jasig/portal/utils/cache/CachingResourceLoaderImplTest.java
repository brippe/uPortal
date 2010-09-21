/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.utils.cache;

import static org.easymock.EasyMock.anyLong;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.apache.commons.io.IOUtils;
import org.jasig.portal.utils.cache.resource.CachedResource;
import org.jasig.portal.utils.cache.resource.CachingResourceLoaderImpl;
import org.jasig.portal.utils.cache.resource.ResourceBuilder;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CachingResourceLoaderImplTest {
    private static File doc1;
    
    @BeforeClass
    public static void setupResources() throws Exception {
        final InputStream doc1In = CachingResourceLoaderImplTest.class.getResourceAsStream("CachingResourceLoaderImplTest_doc1.txt");
        doc1 = File.createTempFile("CachingResourceLoaderImplTest_doc1.", ".txt");
        
        final FileOutputStream doc1Out = new FileOutputStream(doc1);
        IOUtils.copy(doc1In, doc1Out);
        IOUtils.closeQuietly(doc1In);
        IOUtils.closeQuietly(doc1Out);
        doc1.deleteOnExit();
        
    }
    
    @Test
    public void testUncachedLoadNoDigest() throws Exception {
        final Resource doc1Resouce = new FileSystemResource(doc1);
        
        final CachingResourceLoaderImpl loader = new CachingResourceLoaderImpl();
        loader.setDigestInput(false);
        
        final Ehcache cache = createMock(Ehcache.class);
        
        expect(cache.getInternalContext()).andReturn(null);
        expect(cache.get(doc1Resouce)).andReturn(null);
        expect(cache.getQuiet(doc1Resouce)).andReturn(null);
        cache.put(anyObject(Element.class));
        expectLastCall();
        
        replay(cache);
        
        loader.setResourceCache(cache);
        
        final CachedResource<String> cachedResource1 = loader.getResource(doc1Resouce, StringResourceBuilder.INSTANCE);
        
        verify(cache);
        
        assertNotNull(cachedResource1);
        assertEquals(null, cachedResource1.getDigestAlgorithm());
        assertEquals(null, cachedResource1.getLastLoadDigest());
        assertEquals("My Test Resource", cachedResource1.getCachedResource());
    }
    
    @Test
    public void testUncachedLoad() throws Exception {
        final Resource doc1Resouce = new FileSystemResource(doc1);

        final CachingResourceLoaderImpl loader = new CachingResourceLoaderImpl();
        
        final Ehcache cache = createMock(Ehcache.class);
        
        expect(cache.getInternalContext()).andReturn(null);
        expect(cache.get(doc1Resouce)).andReturn(null);
        expect(cache.getQuiet(doc1Resouce)).andReturn(null);
        cache.put(anyObject(Element.class));
        expectLastCall();
        
        replay(cache);
        
        loader.setResourceCache(cache);
        
        final CachedResource<String> cachedResource1 = loader.getResource(doc1Resouce, StringResourceBuilder.INSTANCE);
        
        verify(cache);
        
        assertNotNull(cachedResource1);
        assertEquals("MD5", cachedResource1.getDigestAlgorithm());
        assertEquals("-1zbTdqQDChT-t3UTtOLew", cachedResource1.getLastLoadDigest());
        assertEquals("My Test Resource", cachedResource1.getCachedResource());
    }
    
    @Test
    public void testCachedModifiedLoad() throws Exception {
        final Resource doc1Resouce = new FileSystemResource(doc1);

        final CachingResourceLoaderImpl loader = new CachingResourceLoaderImpl();
        
        final Ehcache cache = createMock(Ehcache.class);
        final CachedResource<?> cachedResource = createMock(CachedResource.class);
        
        expect(cache.getInternalContext()).andReturn(null);
        expect(cache.get(doc1Resouce))
            .andReturn(new Element(doc1Resouce, cachedResource));
        
        expect(cachedResource.getLastCheckTime()).andReturn(1000000L);
        expect(cachedResource.getLastLoadTime()).andReturn(1000000L);
        
        cache.put(anyObject(Element.class));
        expectLastCall();
        
        replay(cache, cachedResource);
        
        loader.setResourceCache(cache);
        
        doc1.setLastModified(2000000);
        
        final CachedResource<String> cachedResource1 = loader.getResource(doc1Resouce, StringResourceBuilder.INSTANCE);
        
        verify(cache, cachedResource);
        
        assertNotNull(cachedResource1);
        assertEquals("MD5", cachedResource1.getDigestAlgorithm());
        assertEquals("-1zbTdqQDChT-t3UTtOLew", cachedResource1.getLastLoadDigest());
        assertEquals("My Test Resource", cachedResource1.getCachedResource());
    }
    
    @Test
    public void testCachedNotModified() throws Exception {
        final Resource doc1Resouce = new FileSystemResource(doc1);
        
        final CachingResourceLoaderImpl loader = new CachingResourceLoaderImpl();
        
        final Ehcache cache = createMock(Ehcache.class);
        final CachedResource<?> cachedResource = createMock(CachedResource.class);
        
        expect(cache.getInternalContext()).andReturn(null);
        final Element element = new Element("class path resource [CachingResourceLoaderImplTest_doc1.txt]", cachedResource);
        expect(cache.get(doc1Resouce)).andReturn(element);
        
        expect(cachedResource.getLastCheckTime()).andReturn(1000001L);
        expect(cachedResource.getLastLoadTime()).andReturn(1000001L);
        cachedResource.setLastCheckTime(anyLong());
        cache.put(element);
        expectLastCall();
        
        replay(cache, cachedResource);
        
        loader.setResourceCache(cache);
        
        doc1.setLastModified(1000000);
        
        final CachedResource<String> cachedResource1 = loader.getResource(doc1Resouce, StringResourceBuilder.INSTANCE);
        
        verify(cache, cachedResource);
        
        assertNotNull(cachedResource1);
        assertTrue(cachedResource1 == cachedResource);
    }
    
    @Test
    public void testCachedWithinInterval() throws Exception {
        final Resource doc1Resouce = new FileSystemResource(doc1);
        
        final CachingResourceLoaderImpl loader = new CachingResourceLoaderImpl();
        
        final Ehcache cache = createMock(Ehcache.class);
        final CachedResource<?> cachedResource = createMock(CachedResource.class);
        
        expect(cache.getInternalContext()).andReturn(null);
        expect(cache.get(doc1Resouce))
            .andReturn(new Element(doc1Resouce, cachedResource));
        
        expect(cachedResource.getLastCheckTime()).andReturn(System.currentTimeMillis());
        
        replay(cache, cachedResource);
        
        loader.setResourceCache(cache);
        
        final CachedResource<String> cachedResource1 = loader.getResource(doc1Resouce, StringResourceBuilder.INSTANCE);
        
        verify(cache, cachedResource);
        
        assertNotNull(cachedResource1);
        assertTrue(cachedResource1 == cachedResource);
    }
    
    private static class StringResourceBuilder implements ResourceBuilder<String> {
        public static final StringResourceBuilder INSTANCE = new StringResourceBuilder();

        @Override
        public String buildResource(Resource resource, InputStream stream) throws IOException {
            return IOUtils.toString(stream);
        }
    }
}