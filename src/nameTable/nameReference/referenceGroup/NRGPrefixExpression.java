package nameTable.nameReference.referenceGroup;

import nameTable.nameReference.NameReference;
import nameTable.nameScope.NameScope;
import util.SourceCodeLocation;

/**
 * The name reference group corresponds to prefix expression. 
 * <p>		PrefixExpression: PrefixOperator Expression 
 * @author Zhou Xiaocong
 * @since 2013-3-13
 * @version 1.0
 */
public class NRGPrefixExpression extends NameReferenceGroup {

	public NRGPrefixExpression(String name, SourceCodeLocation location) {
		super(name, location);
	}

	public NRGPrefixExpression(String name, SourceCodeLocation location, NameScope scope) {
		super(name, location, scope);
	}

	@Override
	public NameReferenceGroupKind getGroupKind() {
		return NameReferenceGroupKind.NRGK_PREFIX_EXPRESSION;
	}
	
	@Override
	public boolean resolveBinding() {
		if (definition != null) return true;

		if (subreferences != null) {
			for (NameReference reference : subreferences) reference.resolveBinding();
		}
		if (subreferences.size() > 0) {
			NameReference firstRef = subreferences.get(0);
			bindTo(getResultTypeDefinition(firstRef));
		}

		return isResolved();
	}
}
