/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.internal.core;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.jsdt.core.IClassFile;
import org.eclipse.wst.jsdt.core.ICompilationUnit;
import org.eclipse.wst.jsdt.core.IJavaElement;
import org.eclipse.wst.jsdt.core.IJavaModelStatusConstants;
import org.eclipse.wst.jsdt.core.IJavaProject;
import org.eclipse.wst.jsdt.core.IPackageFragment;
import org.eclipse.wst.jsdt.core.IPackageFragmentRoot;
import org.eclipse.wst.jsdt.core.IParent;
import org.eclipse.wst.jsdt.core.ISourceManipulation;
import org.eclipse.wst.jsdt.core.JavaCore;
import org.eclipse.wst.jsdt.core.JavaModelException;
import org.eclipse.wst.jsdt.core.WorkingCopyOwner;
import org.eclipse.wst.jsdt.internal.compiler.util.SuffixConstants;
import org.eclipse.wst.jsdt.internal.core.JavaModelManager.PerProjectInfo;
import org.eclipse.wst.jsdt.internal.core.util.MementoTokenizer;
import org.eclipse.wst.jsdt.internal.core.util.Messages;
import org.eclipse.wst.jsdt.internal.core.util.Util;

/**
 * @see IPackageFragment
 */
public class PackageFragment extends Openable implements IPackageFragment, SuffixConstants {
	/**
	 * Constant empty list of class files
	 */
	protected static final IClassFile[] NO_CLASSFILES = new IClassFile[] {};
	/**
	 * Constant empty list of compilation units
	 */
	protected static final ICompilationUnit[] NO_COMPILATION_UNITS = new ICompilationUnit[] {};
	
	public String[] names;

protected PackageFragment(PackageFragmentRoot root, String[] names) {
	super(root);
	this.names = names;
}
/**
 * @see Openable
 */
protected boolean buildStructure(OpenableElementInfo info, IProgressMonitor pm, Map newElements, IResource underlyingResource) throws JavaModelException {

	// check whether this pkg can be opened
	if (!underlyingResource.isAccessible()) throw newNotPresentException();
	
	// check that it is not excluded (https://bugs.eclipse.org/bugs/show_bug.cgi?id=138577)
	int kind = getKind();
	if (kind == IPackageFragmentRoot.K_SOURCE && Util.isExcluded(this)) 
		throw newNotPresentException();


	// add compilation units/class files from resources
	HashSet vChildren = new HashSet();
	try {
	    PackageFragmentRoot root = getPackageFragmentRoot();
		char[][] inclusionPatterns = root.fullInclusionPatternChars();
		char[][] exclusionPatterns = root.fullExclusionPatternChars();
		IResource[] members = ((IContainer) underlyingResource).members();
		int length = members.length;
		if (length > 0) {
			IJavaProject project = getJavaProject();
			String sourceLevel = project.getOption(JavaCore.COMPILER_SOURCE, true);
			String complianceLevel = project.getOption(JavaCore.COMPILER_COMPLIANCE, true);
			for (int i = 0; i < length; i++) {
				IResource child = members[i];
				if (child.getType() != IResource.FOLDER
						&& !Util.isExcluded(child, inclusionPatterns, exclusionPatterns)) {
					IJavaElement childElement;
					if (kind == IPackageFragmentRoot.K_SOURCE && Util.isValidCompilationUnitName(child.getName(), sourceLevel, complianceLevel)) {
						childElement = new CompilationUnit(this, child.getName(), DefaultWorkingCopyOwner.PRIMARY);
						vChildren.add(childElement);
					} else if (kind == IPackageFragmentRoot.K_BINARY && Util.isValidClassFileName(child.getName(), sourceLevel, complianceLevel)) {
						childElement = getClassFile(child.getName());
						vChildren.add(childElement);
					}
				}
			}
		}
	} catch (CoreException e) {
		throw new JavaModelException(e);
	}
	
	if (kind == IPackageFragmentRoot.K_SOURCE) {
		// add primary compilation units
		ICompilationUnit[] primaryCompilationUnits = getCompilationUnits(DefaultWorkingCopyOwner.PRIMARY);
		for (int i = 0, length = primaryCompilationUnits.length; i < length; i++) {
			ICompilationUnit primary = primaryCompilationUnits[i];
			vChildren.add(primary);
		}
	}
	
	IJavaElement[] children = new IJavaElement[vChildren.size()];
	vChildren.toArray(children);
	info.setChildren(children);
	return true;
}
/**
 * Returns true if this fragment contains at least one java resource.
 * Returns false otherwise.
 */
public boolean containsJavaResources() throws JavaModelException {
	return ((PackageFragmentInfo) getElementInfo()).containsJavaResources();
}
/**
 * @see ISourceManipulation
 */
public void copy(IJavaElement container, IJavaElement sibling, String rename, boolean force, IProgressMonitor monitor) throws JavaModelException {
	if (container == null) {
		throw new IllegalArgumentException(Messages.operation_nullContainer); 
	}
	IJavaElement[] elements= new IJavaElement[] {this};
	IJavaElement[] containers= new IJavaElement[] {container};
	IJavaElement[] siblings= null;
	if (sibling != null) {
		siblings= new IJavaElement[] {sibling};
	}
	String[] renamings= null;
	if (rename != null) {
		renamings= new String[] {rename};
	}
	getJavaModel().copy(elements, containers, siblings, renamings, force, monitor);
}
/**
 * @see IPackageFragment
 */
public ICompilationUnit createCompilationUnit(String cuName, String contents, boolean force, IProgressMonitor monitor) throws JavaModelException {
	CreateCompilationUnitOperation op= new CreateCompilationUnitOperation(this, cuName, contents, force);
	op.runOperation(monitor);
	return new CompilationUnit(this, cuName, DefaultWorkingCopyOwner.PRIMARY);
}
/**
 * @see JavaElement
 */
protected Object createElementInfo() {
	return new PackageFragmentInfo();
}
/**
 * @see ISourceManipulation
 */
public void delete(boolean force, IProgressMonitor monitor) throws JavaModelException {
	IJavaElement[] elements = new IJavaElement[] {this};
	getJavaModel().delete(elements, force, monitor);
}
public boolean equals(Object o) {
	if (this == o) return true;
	if (!(o instanceof PackageFragment)) return false;
	
	PackageFragment other = (PackageFragment) o;		
	return Util.equalArraysOrNull(this.names, other.names) &&
			this.parent.equals(other.parent);
}
public boolean exists() {
	// super.exist() only checks for the parent and the resource existence
	// so also ensure that the package is not exceluded (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=138577)
	return super.exists() && !Util.isExcluded(this); 
}
/**
 * @see IPackageFragment#getClassFile(String)
 * @exception IllegalArgumentException if the name does not end with ".class"
 */
public IClassFile getClassFile(String classFileName) {
	if (!org.eclipse.wst.jsdt.internal.compiler.util.Util.isClassFileName(classFileName)) {
		throw new IllegalArgumentException(Messages.element_invalidClassFileName); 
	}
	// don't hold on the .class file extension to save memory
	// also make sure to not use substring as the resulting String may hold on the underlying char[] which might be much bigger than necessary
	int length = classFileName.length() - SUFFIX_CLASS.length;
	char[] nameWithoutExtension = new char[length];
	classFileName.getChars(0, length, nameWithoutExtension, 0);
	return new ClassFile(this, new String(nameWithoutExtension));
}
/**
 * Returns a the collection of class files in this - a folder package fragment which has a root
 * that has its kind set to <code>IPackageFragmentRoot.K_Source</code> does not
 * recognize class files.
 *
 * @see IPackageFragment#getClassFiles()
 */
public IClassFile[] getClassFiles() throws JavaModelException {
	if (getKind() == IPackageFragmentRoot.K_SOURCE) {
		return NO_CLASSFILES;
	}
	
	ArrayList list = getChildrenOfType(CLASS_FILE);
	IClassFile[] array= new IClassFile[list.size()];
	list.toArray(array);
	return array;
}
/**
 * @see IPackageFragment#getCompilationUnit(String)
 * @exception IllegalArgumentException if the name does not end with ".js"
 */
public ICompilationUnit getCompilationUnit(String cuName) {
	if (!org.eclipse.wst.jsdt.internal.core.util.Util.isJavaLikeFileName(cuName)) {
		throw new IllegalArgumentException(Messages.convention_unit_notJavaName); 
	}
	// If parent specified in filename remove it 
	String parentName = new String();
	try {
		IResource parentNameR = this.parent.getResource();
		parentName = parentNameR==null?null:parentNameR.getName() + "/";
		//String parentString = parentName.getProjectRelativePath().toString();
	} catch (Exception ex) {
		
		ex.printStackTrace();
	}
	if(parentName!=null) {
		int pi = cuName.indexOf(parentName);
		if( pi>-1 && pi<2  ) {
			String newCp = "/" + cuName.substring(pi+parentName.length(),cuName.length());
			return new CompilationUnit(this, newCp, DefaultWorkingCopyOwner.PRIMARY);
		}
	}
	return new CompilationUnit(this, cuName, DefaultWorkingCopyOwner.PRIMARY);
}
/**
 * @see IPackageFragment#getCompilationUnits()
 */
public ICompilationUnit[] getCompilationUnits() throws JavaModelException {
	if (getKind() == IPackageFragmentRoot.K_BINARY) {
		return NO_COMPILATION_UNITS;
	}
	
	ArrayList list = getChildrenOfType(COMPILATION_UNIT);
	ICompilationUnit[] array= new ICompilationUnit[list.size()];
	list.toArray(array);
	return array;
}
/**
 * @see IPackageFragment#getCompilationUnits(WorkingCopyOwner)
 */
public ICompilationUnit[] getCompilationUnits(WorkingCopyOwner owner) {
	ICompilationUnit[] workingCopies = JavaModelManager.getJavaModelManager().getWorkingCopies(owner, false/*don't add primary*/);
	if (workingCopies == null) return JavaModelManager.NO_WORKING_COPY;
	int length = workingCopies.length;
	ICompilationUnit[] result = new ICompilationUnit[length];
	int index = 0;
	for (int i = 0; i < length; i++) {
		ICompilationUnit wc = workingCopies[i];
		if (equals(wc.getParent()) && !Util.isExcluded(wc)) { // 59933 - excluded wc shouldn't be answered back
			result[index++] = wc;
		}
	}
	if (index != length) {
		System.arraycopy(result, 0, result = new ICompilationUnit[index], 0, index);
	}
	return result;
}
public String getElementName() {
	//if (this.names.length == 0)
		//return DEFAULT_PACKAGE_NAME;
	return Util.concatWith(this.names, '/');
}
/**
 * @see IJavaElement
 */
public int getElementType() {
	return PACKAGE_FRAGMENT;
}
/*
 * @see JavaElement
 */
public IJavaElement getHandleFromMemento(String token, MementoTokenizer memento, WorkingCopyOwner owner) {
	switch (token.charAt(0)) {
		case JEM_CLASSFILE:
			if (!memento.hasMoreTokens()) return this;
			String classFileName = memento.nextToken();
			JavaElement classFile = (JavaElement)getClassFile(classFileName);
			return classFile.getHandleFromMemento(memento, owner);
		case JEM_COMPILATIONUNIT:
			if (!memento.hasMoreTokens()) return this;
			String cuName = memento.nextToken();
			JavaElement cu = new CompilationUnit(this, cuName, owner);
			return cu.getHandleFromMemento(memento, owner);
	}
	return null;
}
/**
 * @see JavaElement#getHandleMementoDelimiter()
 */
protected char getHandleMementoDelimiter() {
	return JavaElement.JEM_PACKAGEFRAGMENT;
}
/**
 * @see IPackageFragment#getKind()
 */
public int getKind() throws JavaModelException {
	return ((IPackageFragmentRoot)getParent()).getKind();
}
/**
 * Returns an array of non-java resources contained in the receiver.
 */
public Object[] getNonJavaResources() throws JavaModelException {
	if (this.isDefaultPackage()) {
		// We don't want to show non java resources of the default package (see PR #1G58NB8)
		return JavaElementInfo.NO_NON_JAVA_RESOURCES;
	} else {
		return ((PackageFragmentInfo) getElementInfo()).getNonJavaResources(getResource(), getPackageFragmentRoot());
	}
}
/**
 * @see IJavaElement#getPath()
 */
public IPath getPath() {
	PackageFragmentRoot root = this.getPackageFragmentRoot();
	if (root.isArchive()) {
		return root.getPath();
	} else {
		IPath path = root.getPath();
		for (int i = 0, length = this.names.length; i < length; i++) {
			String name = this.names[i];
			path = path.append(name);
		}
		return path;
	}
}
/**
 * @see IJavaElement#getResource()
 */
public IResource getResource() {
	PackageFragmentRoot root = this.getPackageFragmentRoot();
	if (!root.isResourceContainer()) {
		return root.getResource();
	} else {
		int length = this.names.length;
		if (length == 0) {
			return root.getResource();
		} else {
			IPath path = new Path(this.names[0]);
			for (int i = 1; i < length; i++)
				path = path.append(this.names[i]);
			return ((IContainer)root.getResource()).getFolder(path);
		}
	}
}
/**
 * @see IJavaElement#getUnderlyingResource()
 */
public IResource getUnderlyingResource() throws JavaModelException {
	IResource rootResource = this.parent.getUnderlyingResource();
	if (rootResource == null) {
		//jar package fragment root that has no associated resource
		return null;
	}
	// the underlying resource may be a folder or a project (in the case that the project folder
	// is atually the package fragment root)
	if (rootResource.getType() == IResource.FOLDER || rootResource.getType() == IResource.PROJECT) {
		IContainer folder = (IContainer) rootResource;
		String[] segs = this.names;
		for (int i = 0; i < segs.length; ++i) {
			IResource child = folder.findMember(segs[i]);
			if (child == null || child.getType() != IResource.FOLDER) {
				throw newNotPresentException();
			}
			folder = (IFolder) child;
		}
		return folder;
	} else {
		return rootResource;
	}
}
public int hashCode() {
	int hash = this.parent.hashCode();
	for (int i = 0, length = this.names.length; i < length; i++)
		hash = Util.combineHashCodes(this.names[i].hashCode(), hash);
	return hash;
}
/**
 * @see IParent 
 */
public boolean hasChildren() throws JavaModelException {
	return getChildren().length > 0;
}
/**
 * @see IPackageFragment#hasSubpackages()
 */
public boolean hasSubpackages() throws JavaModelException {
	IJavaElement[] packages= ((IPackageFragmentRoot)getParent()).getChildren();
	int namesLength = this.names.length;
	nextPackage: for (int i= 0, length = packages.length; i < length; i++) {
		String[] otherNames = ((PackageFragment) packages[i]).names;
		if (otherNames.length <= namesLength) continue nextPackage;
		for (int j = 0; j < namesLength; j++)
			if (!this.names[j].equals(otherNames[j]))
				continue nextPackage;
		return true;
	}
	return false;
}
/**
 * @see IPackageFragment#isDefaultPackage()
 */
public boolean isDefaultPackage() {
	return this.names.length == 0;
}
/**
 * @see ISourceManipulation#move(IJavaElement, IJavaElement, String, boolean, IProgressMonitor)
 */
public void move(IJavaElement container, IJavaElement sibling, String rename, boolean force, IProgressMonitor monitor) throws JavaModelException {
	if (container == null) {
		throw new IllegalArgumentException(Messages.operation_nullContainer); 
	}
	IJavaElement[] elements= new IJavaElement[] {this};
	IJavaElement[] containers= new IJavaElement[] {container};
	IJavaElement[] siblings= null;
	if (sibling != null) {
		siblings= new IJavaElement[] {sibling};
	}
	String[] renamings= null;
	if (rename != null) {
		renamings= new String[] {rename};
	}
	getJavaModel().move(elements, containers, siblings, renamings, force, monitor);
}
/**
 * @see ISourceManipulation#rename(String, boolean, IProgressMonitor)
 */
public void rename(String newName, boolean force, IProgressMonitor monitor) throws JavaModelException {
	if (newName == null) {
		throw new IllegalArgumentException(Messages.element_nullName); 
	}
	IJavaElement[] elements= new IJavaElement[] {this};
	IJavaElement[] dests= new IJavaElement[] {this.getParent()};
	String[] renamings= new String[] {newName};
	getJavaModel().rename(elements, dests, renamings, force, monitor);
}
/**
 * Debugging purposes
 */
protected void toStringChildren(int tab, StringBuffer buffer, Object info) {
	if (tab == 0) {
		super.toStringChildren(tab, buffer, info);
	}
}
/**
 * Debugging purposes
 */
protected void toStringInfo(int tab, StringBuffer buffer, Object info, boolean showResolvedInfo) {
	buffer.append(this.tabString(tab));
	if (this.names.length == 0) {
		buffer.append("<default>"); //$NON-NLS-1$
	} else {
		toStringName(buffer);
	}
	if (info == null) {
		buffer.append(" (not open)"); //$NON-NLS-1$
	} else {
		if (tab > 0) {
			buffer.append(" (...)"); //$NON-NLS-1$
		}
	}
}
/*
 * @see IJavaElement#getAttachedJavadoc(IProgressMonitor)
 */
public String getAttachedJavadoc(IProgressMonitor monitor) throws JavaModelException {
	PerProjectInfo projectInfo = JavaModelManager.getJavaModelManager().getPerProjectInfoCheckExistence(this.getJavaProject().getProject());
	String cachedJavadoc = null;
	synchronized (projectInfo.javadocCache) {
		cachedJavadoc = (String) projectInfo.javadocCache.get(this);
	}
	if (cachedJavadoc != null) {
		return cachedJavadoc;
	}
	URL baseLocation= getJavadocBaseLocation();
	if (baseLocation == null) {
		return null;
	}
	StringBuffer pathBuffer = new StringBuffer(baseLocation.toExternalForm());

	if (!(pathBuffer.charAt(pathBuffer.length() - 1) == '/')) {
		pathBuffer.append('/');
	}
	String packPath= this.getElementName().replace('.', '/');
	pathBuffer.append(packPath).append('/').append(JavadocConstants.PACKAGE_FILE_NAME);
	
	if (monitor != null && monitor.isCanceled()) throw new OperationCanceledException();
	final String contents = getURLContents(String.valueOf(pathBuffer));
	if (monitor != null && monitor.isCanceled()) throw new OperationCanceledException();
	if (contents == null) throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.CANNOT_RETRIEVE_ATTACHED_JAVADOC, this));
	synchronized (projectInfo.javadocCache) {
		projectInfo.javadocCache.put(this, contents);
	}
	return contents;
}
}
