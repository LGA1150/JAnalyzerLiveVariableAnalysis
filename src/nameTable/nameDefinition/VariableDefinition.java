package nameTable.nameDefinition;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import nameTable.nameReference.ParameterizedTypeReference;
import nameTable.nameReference.TypeReference;
import nameTable.nameScope.NameScope;
import util.SourceCodeLocation;

/**
 * The class represent a variable definition or a method parameter definition
 * @author Zhou Xiaocong
 * @since 2013-2-21
 * @version 1.0
 * 
 * @update 2013-12-29
 * 		Add method getTypeDefinition()
 */
public class VariableDefinition extends NameDefinition {
	private NameDefinitionKind kind = NameDefinitionKind.NDK_VARIABLE;
	private TypeReference type = null;
	
	public VariableDefinition(String simpleName, String fullQualifiedName, SourceCodeLocation location, NameScope scope) {
		super(simpleName, fullQualifiedName, location, scope);
	}

	/* (non-Javadoc)
	 * @see nameTable.NameDefinition#getNameDefinitionKind()
	 */
	@Override
	public NameDefinitionKind getDefinitionKind() {
		return kind;
	}

	/**
	 * Set the kind of the variable definition, which can be NDK_PARAMETER or NDK_VARIABLE
	 */
	public void setVariableDefinitionKind(NameDefinitionKind kind) {
		this.kind = kind;
	}

	/**
	 * Return the reference of the type declared for the variable or parameter
	 */
	public TypeReference getType() {
		return type;
	}

	/**
	 * Return the definition of the type declared for the variable or parameter
	 */
	public TypeDefinition getTypeDefinition() {
		if (type == null) return null;
		type.resolveBinding();
		return (TypeDefinition)type.getDefinition();
	}
	
	/**
	 * @param flag: if flag == true, then we return its main type and its parameter types when the type of the variable
	 * is a parameterized type, otherwise we only return its main type
	 * @return the possible list of type definition of the field type. If the type of the variable is a parameterized
	 * type, then we return its main type and its parameter types.
	 */
	public List<TypeDefinition> getTypeDefinition(boolean flag) {
		if (type == null) return null;
		type.resolveBinding();
		if (flag == false || !type.isParameterizedType()) {
			List<TypeDefinition> resultList = new ArrayList<TypeDefinition>();
			TypeDefinition variableType = (TypeDefinition)type.getDefinition(); 
			if (variableType != null) resultList.add(variableType);
			return resultList;
		}
		
		ParameterizedTypeReference reference = (ParameterizedTypeReference)type;
		return reference.getDefinition(true);
	}

	/**
	 * Set the type declared of the variable or parameter
	 */
	public void setType(TypeReference type) {
		this.type = type;
	}

	@Override
	public void printDefinitions(PrintWriter writer, int indent) {
		// Create a space string for indent;
		char[] indentArray = new char[indent];
		for (int index = 0; index < indentArray.length; index++) indentArray[index] = '\t';
		String indentString = new String(indentArray);

		StringBuffer buffer = new StringBuffer(indentString + "Variable: ");
		String typeString = type.getName();
		for (int count = 0; count < type.getDimension(); count++) typeString += "[]";
		buffer.append(typeString + " " + simpleName + "\n");

		writer.print(buffer);
	}
	
	/**
	 * Display a variable definition as "type[] variableName"
	 */
	public String toDeclarationString() {
		StringBuffer buffer = new StringBuffer();
		String typeString = type.getName();
		for (int count = 0; count < type.getDimension(); count++) typeString += "[]";
		buffer.append(typeString + " " + simpleName);
		return buffer.toString();
	}
}
