package com.microsoft.alm.plugin.idea.tfvc.core.tfs;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.LocalFilePath;
import com.microsoft.alm.plugin.external.models.Workspace;
import com.microsoft.alm.plugin.external.utils.CommandUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CommandUtils.class})
public class TFVCUtilTest {

    private final Workspace workspace = new Workspace(
            "server",
            "name",
            "computer",
            "owner",
            "comment",
            Collections.singletonList(new Workspace.Mapping("serverPath", "/tmp/localPath", false)));

    @Mock
    private Project mockProject;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(CommandUtils.class);
        when(CommandUtils.getPartialWorkspace(mockProject)).thenReturn(workspace);
    }

    @Test
    public void isFileUnderTFVCMappingShouldTests() {
        Assert.assertTrue(TFVCUtil.isFileUnderTFVCMapping(mockProject, new LocalFilePath("/tmp/localPath/1.txt", false)));
        Assert.assertTrue(TFVCUtil.isFileUnderTFVCMapping(mockProject, new LocalFilePath("/tmp/localPath", true)));
        Assert.assertFalse(TFVCUtil.isFileUnderTFVCMapping(mockProject, new LocalFilePath("/tmp/localPath1", true)));
        Assert.assertFalse(TFVCUtil.isFileUnderTFVCMapping(mockProject, new LocalFilePath("/tmp/localPath1/1.txt", false)));
    }

    @Test
    public void filterValidTFVCPathsTest() {
        List<FilePath> localFiles = Arrays.asList(
                new LocalFilePath("/tmp/localPath", true),
                new LocalFilePath("/tmp/localPath/1.txt", false));
        List<FilePath> nonLocalFiles = Arrays.asList(
                new LocalFilePath("/tmp/localPath1", true),
                new LocalFilePath("/tmp/localPath1/1.txt", false));
        List<FilePath> allPaths = Lists.newArrayList(Iterables.concat(localFiles, nonLocalFiles));
        List<String> localFilePaths = localFiles.stream().map(FilePath::getPath).collect(Collectors.toList());
        assertThat(TFVCUtil.filterValidTFVCPaths(mockProject, allPaths), is(localFilePaths));
    }

    @Test
    public void filterDollarTFVCPathsTest() {
        List<FilePath> localFiles = Arrays.asList(
                new LocalFilePath("/tmp/localPath", true),
                new LocalFilePath("/tmp/localPath/1.txt", false));
        List<FilePath> dollarFiles = Arrays.asList(
                new LocalFilePath("/tmp/localPath/$1.txt", false),
                new LocalFilePath("/tmp/localPath/$1/1.txt", false),
                new LocalFilePath("/tmp/localPath/$1", true));
        List<FilePath> allPaths = Lists.newArrayList(Iterables.concat(localFiles, dollarFiles));
        List<String> localFilePaths = localFiles.stream().map(FilePath::getPath).collect(Collectors.toList());
        assertThat(TFVCUtil.filterValidTFVCPaths(mockProject, allPaths), is(localFilePaths));
    }
}
