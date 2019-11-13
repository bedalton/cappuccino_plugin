package cappuccino.ide.intellij.plugin.comments.parser

/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

import cappuccino.ide.intellij.plugin.comments.lexer.ObjJDocTokens
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType

class ObjJDocParser : PsiParser {
    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        val rootMarker = builder.mark()
        if (builder.tokenType === ObjJDocTokens.START) {
            builder.advanceLexer()
        }
        var currentSectionMarker: PsiBuilder.Marker? = builder.mark()

        // todo: parse KDoc tags, markdown, etc...
        while (!builder.eof()) {
            if (builder.tokenType === ObjJDocTokens.TAG_NAME) {
                currentSectionMarker = parseTag(builder, currentSectionMarker)
            } else if (builder.tokenType === ObjJDocTokens.END) {
                builder.advanceLexer()
            } else {
                builder.advanceLexer()
            }
        }

        currentSectionMarker?.done(KDocElementTypes.KDOC_SECTION)
        rootMarker.done(root)
        return builder.treeBuilt
    }

    private fun parseTag(builder: PsiBuilder, currentSectionMarker: PsiBuilder.Marker?): PsiBuilder.Marker? {
        var currentSectionMarker = currentSectionMarker
        val tagName = builder.tokenText
        val knownTag = KDocKnownTag.Companion.findByTagName(tagName)
        if (knownTag != null && knownTag!!.isSectionStart()) {
            currentSectionMarker!!.done(KDocElementTypes.KDOC_SECTION)
            currentSectionMarker = builder.mark()
        }
        val tagStart = builder.mark()
        builder.advanceLexer()

        while (!builder.eof() && !isAtEndOfTag(builder)) {
            builder.advanceLexer()
        }
        tagStart.done(KDocElementTypes.KDOC_TAG)
        return currentSectionMarker
    }

    private fun isAtEndOfTag(builder: PsiBuilder): Boolean {
        if (builder.tokenType === ObjJDocTokens.END) {
            return true
        }
        if (builder.tokenType === ObjJDocTokens.LEADING_ASTERISK) {
            var lookAheadCount = 1
            if (builder.lookAhead(1) === ObjJDocTokens.text) {
                lookAheadCount++
            }
            if (builder.lookAhead(lookAheadCount) === ObjJDocTokens.TAG_NAME) {
                return true
            }
        }
        return false
    }
}