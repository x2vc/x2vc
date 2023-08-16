package org.x2vc.xml.value;

import org.x2vc.schema.ISchemaManager;
import org.x2vc.stylesheet.IStylesheetManager;
import org.x2vc.xml.request.IDocumentRequest;

import com.google.inject.Inject;

/**
 * Standard implementation of {@link IValueGeneratorFactory}.
 */
public class ValueGeneratorFactory implements IValueGeneratorFactory {

	private IStylesheetManager stylesheetManager;
	private ISchemaManager schemaManager;
	private IPrefixSelector prefixSelector;

	/**
	 * @param prefixSelector
	 */
	@Inject
	ValueGeneratorFactory(IStylesheetManager stylesheetManager, ISchemaManager schemaManager,
			IPrefixSelector prefixSelector) {
		super();
		this.stylesheetManager = stylesheetManager;
		this.schemaManager = schemaManager;
		this.prefixSelector = prefixSelector;
	}

	@Override
	public IValueGenerator createValueGenerator(IDocumentRequest request) {
		return new ValueGenerator(this.stylesheetManager, this.schemaManager, this.prefixSelector, request);
	}

}
