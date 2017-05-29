// Copyright 2013-04-01 PlanBase Inc. & Glen Peterson
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.planbase.pdf.layoutmanager;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

// TODO: This is LogicalPage test and should be renamed to that.
public class PdfLayoutMgrTest {
    @Test public void testBasics() throws IOException {
        PdfLayoutMgr pageMgr = PdfLayoutMgr.newRgbPageMgr();
        LogicalPage lp = pageMgr.logicalPageStart();

        // Just testing some default values before potentially merging changes that could make
        // these variable.
        assertEquals(755.0, lp.yPageTop(), 0.000000001);
        assertEquals(230, lp.yPageBottom(), 0.000000001);
        assertEquals(PDRectangle.LETTER.getHeight(), lp.pageWidth(), 0.000000001);

        lp = pageMgr.logicalPageStart(LogicalPage.Orientation.PORTRAIT);

        assertEquals(755.0, lp.yPageTop(), 0.000000001);
        assertEquals(0.0, lp.yPageBottom(), 0.000000001);
        assertEquals(PDRectangle.LETTER.getWidth(), lp.pageWidth(), 0.000000001);
    }
}