package org.pentaho.di.ui.repository.repositoryexplorer.controllers;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryDirectory;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.components.XulPromptBox;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.dom.DocumentFactory;
import org.pentaho.ui.xul.dom.dom4j.ElementDom4J;
import org.pentaho.ui.xul.swt.custom.MessageDialogBase;
import org.pentaho.ui.xul.util.XulDialogCallback;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link BrowseController}.
 */
public class BrowserControllerTest {

  private static final String PROMPTBOX = "promptbox";
  private static final String FOLDER_NAME = "New Folder";

  private static class XulPromptBoxMock extends MessageDialogBase implements XulPromptBox {
    private XulDialogCallback.Status status;

    public XulPromptBoxMock( XulDialogCallback.Status status ) {
      super( PROMPTBOX );
      this.status = status;
    }

    @Override public String getValue() {
      return null;
    }

    @Override public void setValue( String value ) {
    }

    @Override public int open() {
      for ( XulDialogCallback<String> callback : callbacks ) {
        callback.onClose( null, status, FOLDER_NAME );
      }
      return 0;
    }
  }

  private Document document;
  private Binding directoryBinding;
  private Binding selectedItemsBinding;
  private Map<ObjectId, UIRepositoryDirectory> directoryMap;
  private UIRepositoryDirectory selectedFolder;

  private BrowseController controller;

  @Before
  public void setUp() throws Exception {
    DocumentFactory.registerElementClass( ElementDom4J.class );
    UIRepositoryDirectory repositoryDirectory = mock( UIRepositoryDirectory.class );
    XulDomContainer xulDomContainer = mock( XulDomContainer.class );
    XulTree folderTree = mock( XulTree.class );
    document = mock( Document.class );
    directoryMap = new HashMap<>();
    directoryBinding = mock( Binding.class );
    selectedItemsBinding = mock( Binding.class );
    selectedFolder = mock( UIRepositoryDirectory.class );

    when( xulDomContainer.getDocumentRoot() ).thenReturn( document );
    doReturn( Collections.singleton( selectedFolder ) ).when( folderTree ).getSelectedItems();
    when( selectedFolder.createFolder( eq( FOLDER_NAME ) ) ).thenReturn( mock( UIRepositoryDirectory.class ) );

    controller = new BrowseController();
    controller.setFolderTree( folderTree );
    controller.setRepositoryDirectory( repositoryDirectory );
    controller.setXulDomContainer( xulDomContainer );
    controller.setDirMap( directoryMap );
    controller.setDirectoryBinding( directoryBinding );
    controller.setSelectedItemsBinding( selectedItemsBinding );
  }

  /**
   * Test for {@link BrowseController#createFolder()}.
   * <p/>
   * Given an opened folder creation dialog with the non-empty folder name field.
   * <p/>
   * When this prompt dialog is just simply closed by pressing 'x' button, then folder should not be created.
   *
   * @throws Exception
   */
  @Test
  public void shouldNotCreateFolderOnCloseCreationDialog() throws Exception {
    // prepare
    XulPromptBox prompt = new XulPromptBoxMock( XulDialogCallback.Status.CANCEL );
    when( document.createElement( eq( PROMPTBOX ) ) ).thenReturn( prompt );

    // run
    controller.createFolder();

    // assert
    assertTrue( directoryMap.isEmpty() );
    verify( selectedFolder, times( 0 ) ).createFolder( anyString() );
    verify( directoryBinding, times( 0 ) ).fireSourceChanged();
    verify( selectedItemsBinding, times( 0 ) ).fireSourceChanged();
  }

  /**
   * Test for {@link BrowseController#createFolder()}.
   * <p/>
   * Given an opened folder creation dialog with the non-empty folder name field.
   * <p/>
   * When this prompt dialog is accepted, then a folder should be created.
   *
   * @throws Exception
   */
  @Test
  public void shouldCreateFolderOnAcceptCreationDialog() throws Exception {
    // prepare
    XulPromptBox prompt = new XulPromptBoxMock( XulDialogCallback.Status.ACCEPT );
    when( document.createElement( eq( PROMPTBOX ) ) ).thenReturn( prompt );

    // run
    controller.createFolder();

    // assert
    assertFalse( directoryMap.isEmpty() );
    verify( selectedFolder ).createFolder( anyString() );
    verify( directoryBinding ).fireSourceChanged();
    verify( selectedItemsBinding ).fireSourceChanged();
  }
}
