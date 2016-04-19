/*******************************************************************************
* Copyright (c) 2016 Red Hat, Inc.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* 	Contributors:
* 		 Red Hat Inc. - initial API and implementation and/or initial documentation
*******************************************************************************/

package org.eclipse.wst.jsdt.internal.core.search.indexing;

import org.eclipse.wst.jsdt.core.dom.ASTNode;
import org.eclipse.wst.jsdt.core.dom.ASTVisitor;
import org.eclipse.wst.jsdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.wst.jsdt.core.dom.ArrayAccess;
import org.eclipse.wst.jsdt.core.dom.ArrayCreation;
import org.eclipse.wst.jsdt.core.dom.ArrayInitializer;
import org.eclipse.wst.jsdt.core.dom.ArrayType;
import org.eclipse.wst.jsdt.core.dom.Assignment;
import org.eclipse.wst.jsdt.core.dom.Block;
import org.eclipse.wst.jsdt.core.dom.BlockComment;
import org.eclipse.wst.jsdt.core.dom.BooleanLiteral;
import org.eclipse.wst.jsdt.core.dom.BreakStatement;
import org.eclipse.wst.jsdt.core.dom.CatchClause;
import org.eclipse.wst.jsdt.core.dom.CharacterLiteral;
import org.eclipse.wst.jsdt.core.dom.ClassInstanceCreation;
import org.eclipse.wst.jsdt.core.dom.ConditionalExpression;
import org.eclipse.wst.jsdt.core.dom.ConstructorInvocation;
import org.eclipse.wst.jsdt.core.dom.ContinueStatement;
import org.eclipse.wst.jsdt.core.dom.DoStatement;
import org.eclipse.wst.jsdt.core.dom.EmptyStatement;
import org.eclipse.wst.jsdt.core.dom.EnhancedForStatement;
import org.eclipse.wst.jsdt.core.dom.ExpressionStatement;
import org.eclipse.wst.jsdt.core.dom.FieldAccess;
import org.eclipse.wst.jsdt.core.dom.FieldDeclaration;
import org.eclipse.wst.jsdt.core.dom.ForInStatement;
import org.eclipse.wst.jsdt.core.dom.ForOfStatement;
import org.eclipse.wst.jsdt.core.dom.ForStatement;
import org.eclipse.wst.jsdt.core.dom.FunctionDeclaration;
import org.eclipse.wst.jsdt.core.dom.FunctionExpression;
import org.eclipse.wst.jsdt.core.dom.FunctionInvocation;
import org.eclipse.wst.jsdt.core.dom.FunctionRef;
import org.eclipse.wst.jsdt.core.dom.FunctionRefParameter;
import org.eclipse.wst.jsdt.core.dom.IfStatement;
import org.eclipse.wst.jsdt.core.dom.ImportDeclaration;
import org.eclipse.wst.jsdt.core.dom.InferredType;
import org.eclipse.wst.jsdt.core.dom.InfixExpression;
import org.eclipse.wst.jsdt.core.dom.Initializer;
import org.eclipse.wst.jsdt.core.dom.InstanceofExpression;
import org.eclipse.wst.jsdt.core.dom.JSdoc;
import org.eclipse.wst.jsdt.core.dom.JavaScriptUnit;
import org.eclipse.wst.jsdt.core.dom.LabeledStatement;
import org.eclipse.wst.jsdt.core.dom.LineComment;
import org.eclipse.wst.jsdt.core.dom.ListExpression;
import org.eclipse.wst.jsdt.core.dom.MemberRef;
import org.eclipse.wst.jsdt.core.dom.Modifier;
import org.eclipse.wst.jsdt.core.dom.NullLiteral;
import org.eclipse.wst.jsdt.core.dom.NumberLiteral;
import org.eclipse.wst.jsdt.core.dom.ObjectLiteral;
import org.eclipse.wst.jsdt.core.dom.ObjectLiteralField;
import org.eclipse.wst.jsdt.core.dom.PackageDeclaration;
import org.eclipse.wst.jsdt.core.dom.ParenthesizedExpression;
import org.eclipse.wst.jsdt.core.dom.PostfixExpression;
import org.eclipse.wst.jsdt.core.dom.PrefixExpression;
import org.eclipse.wst.jsdt.core.dom.PrimitiveType;
import org.eclipse.wst.jsdt.core.dom.QualifiedName;
import org.eclipse.wst.jsdt.core.dom.QualifiedType;
import org.eclipse.wst.jsdt.core.dom.RegularExpressionLiteral;
import org.eclipse.wst.jsdt.core.dom.ReturnStatement;
import org.eclipse.wst.jsdt.core.dom.SimpleName;
import org.eclipse.wst.jsdt.core.dom.SimpleType;
import org.eclipse.wst.jsdt.core.dom.SingleVariableDeclaration;
import org.eclipse.wst.jsdt.core.dom.StringLiteral;
import org.eclipse.wst.jsdt.core.dom.SuperConstructorInvocation;
import org.eclipse.wst.jsdt.core.dom.SuperFieldAccess;
import org.eclipse.wst.jsdt.core.dom.SuperMethodInvocation;
import org.eclipse.wst.jsdt.core.dom.SwitchCase;
import org.eclipse.wst.jsdt.core.dom.SwitchStatement;
import org.eclipse.wst.jsdt.core.dom.TagElement;
import org.eclipse.wst.jsdt.core.dom.TextElement;
import org.eclipse.wst.jsdt.core.dom.ThisExpression;
import org.eclipse.wst.jsdt.core.dom.ThrowStatement;
import org.eclipse.wst.jsdt.core.dom.TryStatement;
import org.eclipse.wst.jsdt.core.dom.TypeDeclaration;
import org.eclipse.wst.jsdt.core.dom.TypeDeclarationStatement;
import org.eclipse.wst.jsdt.core.dom.TypeLiteral;
import org.eclipse.wst.jsdt.core.dom.UndefinedLiteral;
import org.eclipse.wst.jsdt.core.dom.VariableDeclarationExpression;
import org.eclipse.wst.jsdt.core.dom.VariableDeclarationFragment;
import org.eclipse.wst.jsdt.core.dom.VariableDeclarationStatement;
import org.eclipse.wst.jsdt.core.dom.WhileStatement;
import org.eclipse.wst.jsdt.core.dom.WithStatement;

/**
 * @author Angel Misevski
 *
 */
public class ASTViewGenerator extends ASTVisitor {

	int indent;

	public ASTViewGenerator() {
		super(false);
		indent=-1;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#preVisit(org.eclipse.wst.jsdt.core.dom.ASTNode)
	 */
	public void preVisit(ASTNode node) {

		super.preVisit(node);
		indent++;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#postVisit(org.eclipse.wst.jsdt.core.dom.ASTNode)
	 */
	public void postVisit(ASTNode node) {

		super.postVisit(node);
		indent--;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.AnonymousClassDeclaration)
	 */
	public boolean visit(AnonymousClassDeclaration node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.ArrayAccess)
	 */
	public boolean visit(ArrayAccess node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.ArrayCreation)
	 */
	public boolean visit(ArrayCreation node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.ArrayInitializer)
	 */
	public boolean visit(ArrayInitializer node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.ArrayType)
	 */
	public boolean visit(ArrayType node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.Assignment)
	 */
	public boolean visit(Assignment node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.Block)
	 */
	public boolean visit(Block node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.BlockComment)
	 */
	public boolean visit(BlockComment node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.BooleanLiteral)
	 */
	public boolean visit(BooleanLiteral node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.BreakStatement)
	 */
	public boolean visit(BreakStatement node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.CatchClause)
	 */
	public boolean visit(CatchClause node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.CharacterLiteral)
	 */
	public boolean visit(CharacterLiteral node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.RegularExpressionLiteral)
	 */
	public boolean visit(RegularExpressionLiteral node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.ClassInstanceCreation)
	 */
	public boolean visit(ClassInstanceCreation node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.JavaScriptUnit)
	 */
	public boolean visit(JavaScriptUnit node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.ConditionalExpression)
	 */
	public boolean visit(ConditionalExpression node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.ConstructorInvocation)
	 */
	public boolean visit(ConstructorInvocation node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.ContinueStatement)
	 */
	public boolean visit(ContinueStatement node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.DoStatement)
	 */
	public boolean visit(DoStatement node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.EmptyStatement)
	 */
	public boolean visit(EmptyStatement node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.EnhancedForStatement)
	 */
	public boolean visit(EnhancedForStatement node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.ExpressionStatement)
	 */
	public boolean visit(ExpressionStatement node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.FieldAccess)
	 */
	public boolean visit(FieldAccess node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.FieldDeclaration)
	 */
	public boolean visit(FieldDeclaration node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.ForStatement)
	 */
	public boolean visit(ForStatement node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.ForInStatement)
	 */
	public boolean visit(ForInStatement node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.ForOfStatement)
	 */
	public boolean visit(ForOfStatement node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.IfStatement)
	 */
	public boolean visit(IfStatement node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.ImportDeclaration)
	 */
	public boolean visit(ImportDeclaration node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.InferredType)
	 */
	public boolean visit(InferredType node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.InfixExpression)
	 */
	public boolean visit(InfixExpression node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.InstanceofExpression)
	 */
	public boolean visit(InstanceofExpression node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.Initializer)
	 */
	public boolean visit(Initializer node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.JSdoc)
	 */
	public boolean visit(JSdoc node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.LabeledStatement)
	 */
	public boolean visit(LabeledStatement node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.LineComment)
	 */
	public boolean visit(LineComment node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.ListExpression)
	 */
	public boolean visit(ListExpression node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.MemberRef)
	 */
	public boolean visit(MemberRef node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.FunctionRef)
	 */
	public boolean visit(FunctionRef node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.FunctionRefParameter)
	 */
	public boolean visit(FunctionRefParameter node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.FunctionDeclaration)
	 */
	public boolean visit(FunctionDeclaration node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.FunctionInvocation)
	 */
	public boolean visit(FunctionInvocation node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.Modifier)
	 */
	public boolean visit(Modifier node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.NullLiteral)
	 */
	public boolean visit(NullLiteral node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.UndefinedLiteral)
	 */
	public boolean visit(UndefinedLiteral node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.NumberLiteral)
	 */
	public boolean visit(NumberLiteral node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.PackageDeclaration)
	 */
	public boolean visit(PackageDeclaration node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.ParenthesizedExpression)
	 */
	public boolean visit(ParenthesizedExpression node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.PostfixExpression)
	 */
	public boolean visit(PostfixExpression node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.PrefixExpression)
	 */
	public boolean visit(PrefixExpression node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.PrimitiveType)
	 */
	public boolean visit(PrimitiveType node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.QualifiedName)
	 */
	public boolean visit(QualifiedName node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.QualifiedType)
	 */
	public boolean visit(QualifiedType node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.ReturnStatement)
	 */
	public boolean visit(ReturnStatement node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.SimpleName)
	 */
	public boolean visit(SimpleName node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.SimpleType)
	 */
	public boolean visit(SimpleType node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.SingleVariableDeclaration)
	 */
	public boolean visit(SingleVariableDeclaration node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.StringLiteral)
	 */
	public boolean visit(StringLiteral node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.SuperConstructorInvocation)
	 */
	public boolean visit(SuperConstructorInvocation node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.SuperFieldAccess)
	 */
	public boolean visit(SuperFieldAccess node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.SuperMethodInvocation)
	 */
	public boolean visit(SuperMethodInvocation node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.SwitchCase)
	 */
	public boolean visit(SwitchCase node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.SwitchStatement)
	 */
	public boolean visit(SwitchStatement node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.TagElement)
	 */
	public boolean visit(TagElement node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.TextElement)
	 */
	public boolean visit(TextElement node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.ThisExpression)
	 */
	public boolean visit(ThisExpression node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.ThrowStatement)
	 */
	public boolean visit(ThrowStatement node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.TryStatement)
	 */
	public boolean visit(TryStatement node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.TypeDeclaration)
	 */
	public boolean visit(TypeDeclaration node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.TypeDeclarationStatement)
	 */
	public boolean visit(TypeDeclarationStatement node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.TypeLiteral)
	 */
	public boolean visit(TypeLiteral node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.VariableDeclarationExpression)
	 */
	public boolean visit(VariableDeclarationExpression node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.VariableDeclarationStatement)
	 */
	public boolean visit(VariableDeclarationStatement node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.VariableDeclarationFragment)
	 */
	public boolean visit(VariableDeclarationFragment node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.WhileStatement)
	 */
	public boolean visit(WhileStatement node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.WithStatement)
	 */
	public boolean visit(WithStatement node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.ObjectLiteral)
	 */
	public boolean visit(ObjectLiteral node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.ObjectLiteralField)
	 */
	public boolean visit(ObjectLiteralField node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#visit(org.eclipse.wst.jsdt.core.dom.FunctionExpression)
	 */
	public boolean visit(FunctionExpression node) {

		System.out.println(padLeft(getSimpleClassName(node.getClass().toString()) + " -- " + node.toString())); //$NON-NLS-1$
		return super.visit(node);
	}

	private String padLeft(String s) {
		String res = ""; //$NON-NLS-1$
		for (int i = 0; i < indent; i++) {
			res += "| "; //$NON-NLS-1$
		}
		res += s;
		return res;
	}

	private String getSimpleClassName(String s) {
		String[] temp = s.split("\\."); //$NON-NLS-1$
		String res = temp[temp.length -1];
		return res;
	}
}
