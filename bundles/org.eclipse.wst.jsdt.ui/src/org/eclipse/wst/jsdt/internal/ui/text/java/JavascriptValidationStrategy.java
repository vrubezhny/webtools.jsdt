/*******************************************************************************
 * Licensed Materials - Property of IBM
 * © Copyright IBM Corporation 2016. All Rights Reserved.
 * U.S. Government Users Restricted Rights - Use, duplication or disclosure
 * restricted by GSA ADP Schedule Contract with IBM Corp. 
 *******************************************************************************/

package org.eclipse.wst.jsdt.internal.ui.text.java;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.wst.jsdt.internal.ui.JavaScriptPlugin;
import org.eclipse.wst.jsdt.ui.JavaScriptUI;
import org.eclipse.wst.jsdt.ui.text.IJavaScriptPartitions;
import org.eclipse.wst.sse.ui.internal.reconcile.validator.ValidatorBuilder;
import org.eclipse.wst.sse.ui.internal.reconcile.validator.ValidatorMetaData;
import org.eclipse.wst.sse.ui.internal.reconcile.validator.ValidatorStrategy;

/**
 * @author jeremy
 *
 */
public class JavascriptValidationStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {
	private static final boolean DEBUG_VALIDATORS = Boolean.TRUE.toString().equalsIgnoreCase(Platform.getDebugOption("org.eclipse.wst.jsdt.ui/debug/reconcilerValidators")); //$NON-NLS-1$
	private final String SSE_UI_ID = "org.eclipse.wst.sse.ui"; //$NON-NLS-1$

	private IDocument fDocument;
	private ValidatorStrategy fValidatorStrategy;
	/**
	 * true if as you type validation is enabled,
	 * false otherwise
	 * @TODO: do we need a preference for this???
	 */
	private boolean fValidationEnabled = true;
	private ISourceViewer fSourceViewer;
	
	/**
	 * @param editor
	 */
	public JavascriptValidationStrategy(ISourceViewer viewer) {
		this.fSourceViewer = viewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#setDocument(org.eclipse.jface.text.IDocument)
	 */
	public void setDocument(IDocument document) {
		this.fDocument = document;
		if (getValidatorStrategy() != null) {
			getValidatorStrategy().setDocument(document);
		}
	}

	private IDocument getDocument() {
		return fDocument;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#reconcile(org.eclipse.jface.text.reconciler.DirtyRegion, org.eclipse.jface.text.IRegion)
	 */
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		reconcile(dirtyRegion);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#reconcile(org.eclipse.jface.text.IRegion)
	 */
	public void reconcile(IRegion partition) {
		ITypedRegion[] partitions = computePartitioning(partition);
		
		// call the validator strategy once for each effected partition
		DirtyRegion dirty = null;
		for (int i = 0; i < partitions.length; i++) {
			dirty = createDirtyRegion(partitions[i], DirtyRegion.INSERT);

			// [source]validator (extension) for this partition
			if (getValidatorStrategy() != null) {
				getValidatorStrategy().reconcile(partitions[i], dirty);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#setProgressMonitor(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void setProgressMonitor(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#initialReconcile()
	 */
	public void initialReconcile() {
		int length = getDocument().getLength();
		reconcile(new Region(0, length));
	}
	
	/**
	 * Called before reconciling is started.
	 *
	 * 
	 */
	public void aboutToBeReconciled() {
		if (getValidatorStrategy() != null) {
			getValidatorStrategy().beginProcessing();
		}
	}
	
	/**
	 * @param dirtyRegion
	 * @return
	 */
	protected ITypedRegion[] computePartitioning(IRegion dirtyRegion) {
		int drOffset = dirtyRegion.getOffset();
		int drLength = dirtyRegion.getLength();

		return computePartitioning(drOffset, drLength);
	}

	
	protected ITypedRegion[] computePartitioning(int drOffset, int drLength) {
		ITypedRegion[] tr = new ITypedRegion[0];
		IDocument doc = getDocument();
		if (doc != null){
			int docLength = doc.getLength();
	
			if (drOffset > docLength) {
				drOffset = docLength;
				drLength = 0;
			}
			else if (drOffset + drLength > docLength) {
				drLength = docLength - drOffset;
			}
	
			try {
				// dirty region may span multiple partitions
				tr = TextUtilities.computePartitioning(doc, getDocumentPartitioning(), drOffset, drLength, true);
			}
			catch (BadLocationException e) {
				String info = "dr: [" + drOffset + ":" + drLength + "] doc: [" + docLength + "] "; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				IStatus status= new Status(IStatus.ERROR, JavaScriptUI.ID_PLUGIN, IStatus.OK, info, e);  //$NON-NLS-1$
				JavaScriptPlugin.getDefault().getLog().log(status);
				tr = new ITypedRegion[0];
			}
		}
		return tr;
	}
	
	protected DirtyRegion createDirtyRegion(IRegion region, String type) {
		return createDirtyRegion(region.getOffset(),  region.getLength(), type);
	}
	
	protected DirtyRegion createDirtyRegion(int offset, int length, String type) {
		DirtyRegion durty = null;
		IDocument doc = getDocument();

		if (doc != null) {
			// safety for BLE
			int docLen = doc.getLength();
			if (offset > docLen) {
				offset = docLen;
				length = 0;
			}
			else if (offset + length >= docLen)
				length = docLen - offset;
			try {
				durty = new DirtyRegion(offset, length, type, doc.get(offset, length));
			}
			catch (BadLocationException e) {
				String info = "dr: [" + offset + ":" + length + "] doc: [" + docLen + "] "; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				IStatus status= new Status(IStatus.ERROR, JavaScriptUI.ID_PLUGIN, IStatus.OK, info, e);  //$NON-NLS-1$
				JavaScriptPlugin.getDefault().getLog().log(status);
			}
		}
		return durty;
	}
	
	protected String getDocumentPartitioning() {
		return IJavaScriptPartitions.JAVA_PARTITIONING;
	}
	
	protected ISourceViewer getTextViewer() {
		return fSourceViewer;
	}
	
	/**
	 * @return Returns the ValidatorStrategy.
	 */
	protected ValidatorStrategy getValidatorStrategy() {
		ValidatorStrategy validatorStrategy = null;
		if (fValidatorStrategy == null && fValidationEnabled) {
			if (getTextViewer() instanceof ISourceViewer) {
				ISourceViewer viewer = (ISourceViewer) getTextViewer();
				String contentTypeId = null;

				IDocument doc = viewer.getDocument();
				contentTypeId = getContentType(doc);

				if (contentTypeId != null) {
					validatorStrategy = new ValidatorStrategy(viewer, contentTypeId);
					ValidatorBuilder vBuilder = new ValidatorBuilder();
					ValidatorMetaData[] vmds = vBuilder.getValidatorMetaData(SSE_UI_ID);
					List enabledValidators = new ArrayList(1);
					/* if any "must" handle this content type, just add them */
					boolean foundSpecificContentTypeValidators = false;
					for (int i = 0; i < vmds.length; i++) {
						if (vmds[i].mustHandleContentType(contentTypeId)) {
							if (DEBUG_VALIDATORS) {
								String info = contentTypeId + " using specific validator " + vmds[i].getValidatorId(); //$NON-NLS-1$
								IStatus status= new Status(IStatus.INFO, JavaScriptUI.ID_PLUGIN, info);  //$NON-NLS-1$
								JavaScriptPlugin.getDefault().getLog().log(status);
							}
							foundSpecificContentTypeValidators = true;
							enabledValidators.add(vmds[i]);
						}
					}
					if (!foundSpecificContentTypeValidators) {
						for (int i = 0; i < vmds.length; i++) {
							if (vmds[i].canHandleContentType(contentTypeId)) {
								if (DEBUG_VALIDATORS) {
									String info = contentTypeId + " using inherited(?) validator " + vmds[i].getValidatorId(); //$NON-NLS-1$
									IStatus status= new Status(IStatus.INFO, JavaScriptUI.ID_PLUGIN, info);  //$NON-NLS-1$
									JavaScriptPlugin.getDefault().getLog().log(status);
								}
								enabledValidators.add(vmds[i]);
							}
						}
					}
					for (int i = 0; i < enabledValidators.size(); i++) {
						validatorStrategy.addValidatorMetaData((ValidatorMetaData) enabledValidators.get(i));
					}
				}
			}
			fValidatorStrategy = validatorStrategy;
		} else if(fValidatorStrategy != null && fValidationEnabled) {
			validatorStrategy = fValidatorStrategy;
		}
		return validatorStrategy;
	}

	
	protected String getContentType(IDocument doc) {
		if (doc == null)
			return null;

		String contentTypeId = null;

		IContentType ct = null;
		try {
			IContentDescription desc = Platform.getContentTypeManager().getDescriptionFor(new StringReader(doc.get()), null, IContentDescription.ALL);
			if (desc != null) {
				ct = desc.getContentType();
				if (ct != null)
					contentTypeId = ct.getId();
			}
		}
		catch (IOException e) {
			// just bail
		}
		return contentTypeId;
	}
}
