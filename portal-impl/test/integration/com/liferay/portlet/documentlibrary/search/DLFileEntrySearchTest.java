/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portlet.documentlibrary.search;

import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.Sync;
import com.liferay.portal.kernel.test.rule.SynchronousDestinationTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.SearchContextTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.MimeTypesUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.model.BaseModel;
import com.liferay.portal.model.Group;
import com.liferay.portal.search.test.BaseSearchTestCase;
import com.liferay.portal.search.test.TestOrderHelper;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.MainServletTestRule;
import com.liferay.portlet.documentlibrary.model.DLFileEntry;
import com.liferay.portlet.documentlibrary.model.DLFileEntryMetadata;
import com.liferay.portlet.documentlibrary.model.DLFileEntryType;
import com.liferay.portlet.documentlibrary.model.DLFolder;
import com.liferay.portlet.documentlibrary.model.DLFolderConstants;
import com.liferay.portlet.documentlibrary.service.DLAppLocalServiceUtil;
import com.liferay.portlet.documentlibrary.service.DLAppServiceUtil;
import com.liferay.portlet.documentlibrary.service.DLFileEntryLocalServiceUtil;
import com.liferay.portlet.documentlibrary.service.DLFileEntryTypeLocalServiceUtil;
import com.liferay.portlet.documentlibrary.util.test.DLAppTestUtil;
import com.liferay.portlet.dynamicdatamapping.io.DDMFormXSDDeserializerUtil;
import com.liferay.portlet.dynamicdatamapping.model.DDMForm;
import com.liferay.portlet.dynamicdatamapping.model.DDMFormLayout;
import com.liferay.portlet.dynamicdatamapping.model.DDMStructure;
import com.liferay.portlet.dynamicdatamapping.model.DDMTemplate;
import com.liferay.portlet.dynamicdatamapping.service.DDMStructureLocalServiceUtil;
import com.liferay.portlet.dynamicdatamapping.storage.DDMFormValues;
import com.liferay.portlet.dynamicdatamapping.storage.Field;
import com.liferay.portlet.dynamicdatamapping.storage.Fields;
import com.liferay.portlet.dynamicdatamapping.util.DDMIndexerUtil;
import com.liferay.portlet.dynamicdatamapping.util.DDMUtil;
import com.liferay.portlet.dynamicdatamapping.util.FieldsToDDMFormValuesConverterUtil;
import com.liferay.portlet.dynamicdatamapping.util.test.DDMStructureTestUtil;

import java.io.File;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Eudaldo Alonso
 */
@Sync
public class DLFileEntrySearchTest extends BaseSearchTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(), MainServletTestRule.INSTANCE,
			SynchronousDestinationTestRule.INSTANCE);

	@Ignore()
	@Override
	@Test
	public void testLocalizedSearch() throws Exception {
	}

	@Test
	public void testOrderByDDMBooleanField() throws Exception {
		TestOrderHelper orderTestHelper = new DLFileEntrySearchTestOrderHelper(
			group);

		orderTestHelper.testOrderByDDMBooleanField();
	}

	@Test
	public void testOrderByDDMIntegerField() throws Exception {
		TestOrderHelper orderTestHelper = new DLFileEntrySearchTestOrderHelper(
			group);

		orderTestHelper.testOrderByDDMIntegerField();
	}

	@Test
	public void testOrderByDDMNumberField() throws Exception {
		TestOrderHelper orderTestHelper = new DLFileEntrySearchTestOrderHelper(
			group);

		orderTestHelper.testOrderByDDMNumberField();
	}

	@Test
	public void testOrderByDDMTextField() throws Exception {
		TestOrderHelper orderTestHelper = new DLFileEntrySearchTestOrderHelper(
			group);

		orderTestHelper.testOrderByDDMTextField();
	}

	@Ignore()
	@Override
	@Test
	public void testSearchAttachments() throws Exception {
	}

	@Test
	public void testSearchTikaRawMetadata() throws Exception {
		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(group.getGroupId());

		SearchContext searchContext = SearchContextTestUtil.getSearchContext(
			group.getGroupId());

		int initialBaseModelsSearchCount = searchBaseModelsCount(
			getBaseModelClass(), group.getGroupId(), "Word", searchContext);

		String fileName = "OSX_Test.docx";

		InputStream inputStream = getClass().getResourceAsStream(
			"dependencies/" + fileName);

		File file = null;

		try {
			String mimeType = MimeTypesUtil.getContentType(file, fileName);

			file = FileUtil.createTempFile(inputStream);

			DLAppLocalServiceUtil.addFileEntry(
				serviceContext.getUserId(), serviceContext.getScopeGroupId(),
				DLFolderConstants.DEFAULT_PARENT_FOLDER_ID, fileName, mimeType,
				fileName, StringPool.BLANK, StringPool.BLANK, file,
				serviceContext);
		}
		finally {
			FileUtil.delete(file);
		}

		Assert.assertEquals(
			initialBaseModelsSearchCount + 1,
			searchBaseModelsCount(
				getBaseModelClass(), group.getGroupId(), "Word",
				searchContext));
	}

	protected BaseModel<?> addBaseModel(
			String keywords, DDMStructure ddmStructure,
			ServiceContext serviceContext)
		throws Exception {

		_ddmStructure = ddmStructure;

		DLFileEntryType dlFileEntryType =
			DLFileEntryTypeLocalServiceUtil.addFileEntryType(
				TestPropsValues.getUserId(), serviceContext.getScopeGroupId(),
				"Structure", StringPool.BLANK,
				new long[] {ddmStructure.getStructureId()}, serviceContext);

		String content = "Content: Enterprise. Open Source. For Life.";

		Fields fields = new Fields();

		Field textField = new Field(
			ddmStructure.getStructureId(), "name", keywords);

		textField.setDefaultLocale(LocaleUtil.US);

		fields.put(textField);

		DDMFormValues ddmFormValues =
			FieldsToDDMFormValuesConverterUtil.convert(ddmStructure, fields);

		DLAppTestUtil.populateServiceContext(
			serviceContext, dlFileEntryType.getFileEntryTypeId());

		serviceContext.setAttribute(
			DDMFormValues.class.getName() + ddmStructure.getStructureId(),
			ddmFormValues);

		FileEntry fileEntry = DLAppLocalServiceUtil.addFileEntry(
			TestPropsValues.getUserId(), serviceContext.getScopeGroupId(),
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID, "Text.txt",
			ContentTypes.TEXT_PLAIN, RandomTestUtil.randomString(),
			StringPool.BLANK, StringPool.BLANK, content.getBytes(),
			serviceContext);

		return (DLFileEntry)fileEntry.getModel();
	}

	@Override
	protected BaseModel<?> addBaseModelWithDDMStructure(
			BaseModel<?> parentBaseModel, String keywords,
			ServiceContext serviceContext)
		throws Exception {

		String definition = DDMStructureTestUtil.getSampleStructureDefinition(
			"name");

		DDMStructure ddmStructure = DDMStructureTestUtil.addStructure(
			serviceContext.getScopeGroupId(), DLFileEntry.class.getName(),
			definition);

		return addBaseModel(keywords, ddmStructure, serviceContext);
	}

	@Override
	protected BaseModel<?> addBaseModelWithWorkflow(
			BaseModel<?> parentBaseModel, boolean approved, String keywords,
			ServiceContext serviceContext)
		throws Exception {

		DLFolder dlFolder = (DLFolder)parentBaseModel;

		long folderId = DLFolderConstants.DEFAULT_PARENT_FOLDER_ID;

		if (dlFolder != null) {
			folderId = dlFolder.getFolderId();
		}

		FileEntry fileEntry = DLAppTestUtil.addFileEntryWithWorkflow(
			serviceContext.getUserId(), serviceContext.getScopeGroupId(),
			folderId, keywords + ".txt", keywords, approved, serviceContext);

		return (DLFileEntry)fileEntry.getModel();
	}

	@Override
	protected void deleteBaseModel(long primaryKey) throws Exception {
		DLFileEntryLocalServiceUtil.deleteDLFileEntry(primaryKey);
	}

	@Override
	protected void expireBaseModelVersions(
			BaseModel<?> baseModel, boolean expireAllVersions,
			ServiceContext serviceContext)
		throws Exception {

		DLFileEntry dlFileEntry = (DLFileEntry)baseModel;

		if (expireAllVersions) {
			DLAppServiceUtil.deleteFileEntry(dlFileEntry.getFileEntryId());
		}
		else {
			DLAppServiceUtil.deleteFileVersion(
				dlFileEntry.getFileEntryId(), dlFileEntry.getVersion());
		}
	}

	@Override
	protected Class<?> getBaseModelClass() {
		return DLFileEntry.class;
	}

	@Override
	protected String getDDMStructureFieldName() {
		return DDMIndexerUtil.encodeName(
			_ddmStructure.getStructureId(), "name",
			LocaleUtil.getSiteDefault());
	}

	@Override
	protected BaseModel<?> getParentBaseModel(
			BaseModel<?> parentBaseModel, ServiceContext serviceContext)
		throws Exception {

		Folder folder = DLAppServiceUtil.addFolder(
			serviceContext.getScopeGroupId(),
			(Long)parentBaseModel.getPrimaryKeyObj(),
			RandomTestUtil.randomString(_FOLDER_NAME_MAX_LENGTH),
			RandomTestUtil.randomString(), serviceContext);

		return (DLFolder)folder.getModel();
	}

	@Override
	protected BaseModel<?> getParentBaseModel(
			Group group, ServiceContext serviceContext)
		throws Exception {

		Folder folder = DLAppServiceUtil.addFolder(
			serviceContext.getScopeGroupId(),
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			RandomTestUtil.randomString(_FOLDER_NAME_MAX_LENGTH),
			RandomTestUtil.randomString(), serviceContext);

		return (DLFolder)folder.getModel();
	}

	@Override
	protected String getParentBaseModelClassName() {
		return DLFolderConstants.getClassName();
	}

	@Override
	protected String getSearchKeywords() {
		return "Title";
	}

	@Override
	protected boolean isExpirableAllVersions() {
		return true;
	}

	@Override
	protected void moveBaseModelToTrash(long primaryKey) throws Exception {
		DLAppServiceUtil.moveFileEntryToTrash(primaryKey);
	}

	@Override
	protected void moveParentBaseModelToTrash(long primaryKey)
		throws Exception {

		DLAppServiceUtil.moveFolderToTrash(primaryKey);
	}

	@Override
	protected long searchGroupEntriesCount(long groupId, long creatorUserId)
		throws Exception {

		Hits hits =  DLAppServiceUtil.search(
			groupId, creatorUserId, WorkflowConstants.STATUS_APPROVED,
			QueryUtil.ALL_POS, QueryUtil.ALL_POS);

		return hits.getLength();
	}

	@Override
	protected BaseModel<?> updateBaseModel(
			BaseModel<?> baseModel, String keywords,
			ServiceContext serviceContext)
		throws Exception {

		DLFileEntry dlFileEntry = (DLFileEntry)baseModel;

		FileEntry fileEntry = DLAppTestUtil.updateFileEntry(
			serviceContext.getScopeGroupId(), dlFileEntry.getFileEntryId(),
			null, dlFileEntry.getMimeType(), keywords, true, serviceContext);

		return (DLFileEntry)fileEntry.getModel();
	}

	@Override
	protected void updateDDMStructure(ServiceContext serviceContext)
		throws Exception {

		String definition = DDMStructureTestUtil.getSampleStructureDefinition(
			"title");

		DDMForm ddmForm = DDMFormXSDDeserializerUtil.deserialize(definition);

		DDMFormLayout ddmFormLayout = DDMUtil.getDefaultDDMFormLayout(ddmForm);

		DDMStructureLocalServiceUtil.updateStructure(
			_ddmStructure.getStructureId(),
			_ddmStructure.getParentStructureId(), _ddmStructure.getNameMap(),
			_ddmStructure.getDescriptionMap(), ddmForm, ddmFormLayout,
			serviceContext);
	}

	protected class DLFileEntrySearchTestOrderHelper extends TestOrderHelper {

		protected DLFileEntrySearchTestOrderHelper(Group group)
			throws Exception {

			super(group);
		}

		@Override
		protected BaseModel<?> addSearchableAssetEntry(
				BaseModel<?> parentBaseModel, String keywords,
				DDMStructure ddmStructure, DDMTemplate ddmTemplate,
				ServiceContext serviceContext)
			throws Exception {

			return addBaseModel(keywords, ddmStructure, serviceContext);
		}

		@Override
		protected String getSearchableAssetEntryClassName() {
			return getBaseModelClassName();
		}

		@Override
		protected BaseModel<?> getSearchableAssetEntryParentBaseModel(
				Group group, ServiceContext serviceContext)
			throws Exception {

			return getParentBaseModel(group, serviceContext);
		}

		@Override
		protected String getSearchableAssetEntryStructureClassName() {
			return DLFileEntryMetadata.class.getName();
		}

	}

	private static final int _FOLDER_NAME_MAX_LENGTH = 100;

	private DDMStructure _ddmStructure;

}