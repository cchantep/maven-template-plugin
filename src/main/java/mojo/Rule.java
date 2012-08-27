package mojo;

import java.io.File;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * Rule to generate a file from a template.
 *
 * @author Cedric Chantepie (cchantepie@corsaire.fr)
 */
public class Rule {
    // --- Properties ---

    /**
     * Template file
     * [null]
     */
    private File template = null;

    /**
     * Generated file (output one),
     * with ${basedir}/target/generated-sources/maven-template-plugin
     * [null]
     */
    private String output = null;

    /**
     * Filters used to go from templates to output file
     * [null]
     */
    private File[] filters = null;

    // --- Properties accessors ---

    /**
     * Returns used template.
     */
    public File getTemplate() {
	return this.template;
    } // end of getTemplate

    /**
     * Sets template.
     *
     * @param template Template to be used
     */
    public void setTemplate(File template) {
	this.template = template;
    } // end of setTemplate

    /**
     * Returns output.
     */
    public String getOutput() {
	return this.output;
    } // end of getOutput

    /**
     * Sets output.
     *
     * @param output File to be generated
     */
    public void setOutput(String output) {
	this.output = output;
    } // end of setOutput

    /**
     * Returns filters.
     */
    public File[] getFilters() {
	return this.filters;
    } // end of getFilters

    /**
     * Sets filters.
     *
     * @param filters Filters to go from template to output file
     */
    public void setFilters(File[] filters) {
	this.filters = filters;
    } // end of setFilters

    // --- Object implementation ---

    /**
     * {@inheritDoc}
     */
    public String toString() {
	return new ToStringBuilder(this).
	    append("template", this.template).
	    append("output", this.output).
	    append("filters", this.filters).
	    toString();

    } // end of toString

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object o) {
	if (o == null || !(o instanceof Rule)) {
	    return false;
	} // end of if

	Rule other = (Rule) o;

	return new EqualsBuilder().
	    append(this.template, other.template).
	    append(this.output, other.output).
	    append(this.filters, other.filters).
	    isEquals();

    } // end of equals

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
	return new HashCodeBuilder(1, 3).
	    append(this.template).
	    append(this.output).
	    append(this.filters).
	    toHashCode();

    } // end of hashCode
} // end of class Rule
