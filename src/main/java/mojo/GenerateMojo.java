package mojo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileWriter;
import java.io.File;

import java.util.StringTokenizer;
import java.util.Properties;
import java.util.Iterator;
import java.util.List;

import java.lang.reflect.Method;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;

import org.apache.velocity.app.VelocityEngine;

import org.apache.maven.project.MavenProject;

import org.apache.maven.profiles.Profile;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.AbstractMojo;

/**
 * Generate files from templates using velocity.
 *
 * @author Cedric Chantepie
 * @goal generate
 * @phase generate-sources
 */
public class GenerateMojo extends AbstractMojo {
    // --- Properties ---

    /**
     * @parameter default-value="${project}"
     */
    private MavenProject project;

    /**
     * Rules list.
     * @parameter
     */
    private Rule[] rules = null;

    // ---

    /**
     * {@inheritDoc}
     */
    public void execute() 
	throws MojoExecutionException {

	if (this.rules == null ||
	    this.rules.length == 0) {

	    getLog().info("No generate rule found");

	    return;
	} // end of if

	// ---

	String basedir = project.getBasedir().getAbsolutePath();
	VelocityEngine vengine = null;

	try {
	    vengine = getVelocityEngine(basedir);
	} catch (Exception e) {
	    throw new MojoExecutionException("Fails to initialize " +
					     "velocity engine", e);
	} // end of catch

	String generatedPath = 
	    "target" + File.separator +
	    "generated-sources" + File.separator +
	    "maven-template-plugin";
	File targetDir = new File(basedir, generatedPath);
	Properties builtins = new Properties();
	List profiles = this.project.getActiveProfiles();
	Iterator iter = (profiles != null) ? profiles.iterator() : null;
	Method getProps = null;
	
	builtins.put("pom", this.project.getModel());

	Object profile;
	Properties props;
	Iterator pi;
	String key;
	StringTokenizer tok;
	int lt;
	String t;
	String val;
	Properties obj;
	while (iter.hasNext()) {
	    profile = iter.next();

	    if (getProps == null) {
		try {
		    getProps = profile.getClass().
			getMethod("getProperties", new Class[0]);

		} catch (Exception e) {
		    throw new MojoExecutionException("Fails to load properties getter", e);
		} // end of catch
	    } // end of if

	    try {
		props = (Properties) getProps.
		    invoke(profile, new Object[0]);
	    
	    } catch (Exception e) {
		throw new MojoExecutionException("Fails to get profile properties", e);
	    } // end of catch

	    // ---

	    pi = props.keySet().iterator();

	    while (pi.hasNext()) {
		key = (String) pi.next();
		val = props.getProperty(key);
		
		if (key.indexOf(".") == -1) {
		    builtins.put(key, val);

		    continue;
		} // end of if

		// ---

		tok = new StringTokenizer(key, ".");
		lt = tok.countTokens() - 1;
		obj = builtins;

		for (int i = 0; tok.hasMoreTokens(); i++) {
		    t = tok.nextToken();

		    if (i == lt) { // last part
			obj.put(t, val);
			
			continue;
		    } // end of if

		    // ---
			
		    if (!obj.containsKey(t)) {
			obj.put(t, new Properties());
		    } // end of if

		    obj = (Properties) obj.get(t);
		} // end of while
	    } // end of while
	} // end of while

	generatedPath = basedir + 
	    File.separator + generatedPath;

	this.project.addCompileSourceRoot(generatedPath);

	for (int i = 0; i < this.rules.length; i++) {
	    processRule(vengine,
			builtins,
			targetDir, 
			basedir, 
			i, 
			this.rules[i]);

	} // end of for
    } // end of execute

    /**
     */
    private static void processRule(final VelocityEngine vengine,
				    final Properties builtins,
				    final File targetDir,
				    final String basedir, 
				    int index,
				    final Rule rule) 
	throws MojoExecutionException {

	File templateFile = rule.getTemplate();

	if (!templateFile.exists()) {
	    throw new MojoExecutionException("Template file does " +
					     "not exist: " + templateFile);

	} // end of if

	File[] filters = rule.getFilters();

	if (filters == null || filters.length == 0) {
	    throw new MojoExecutionException("No generation filter " +
					     "is specified, at rule " +
					     index);

	} // end of if

	File outputFile = new File(targetDir, rule.getOutput());
	File outputDir = outputFile.getParentFile();

	if (!outputDir.exists()) {
	    try {
		outputDir.mkdirs();
	    } catch (SecurityException e) {
		throw new MojoExecutionException("Fails to prepare output " +
						 "directory: " + outputDir, e);
		
	    } // end of catch
	} // end of if

	Properties props = new Properties();
	FileInputStream fis = null;

	for (int i = 0; i < filters.length; i++) {
	    if (!filters[i].exists()) {
		throw new MojoExecutionException("Filter file does not " +
						 "exist: " + filters[i]);

	    } // end of if

	    try {
		fis = new FileInputStream(filters[i]);

		props.load(fis);
	    } catch (IOException e) {
		throw new MojoExecutionException("Fails to load filter", e);
	    } finally {
		if (fis != null) {
		    try {
			fis.close();
		    } catch (Exception e) {
			e.printStackTrace();
		    } // end of catch
		} // end of if
	    } // end of finally
	} // end of for

	props.putAll(builtins);

	VelocityContext ctx = new VelocityContext(props);
	Template template = null;
	String path = templateFile.getAbsolutePath();

	if (path.startsWith(basedir)) {
	    path = path.substring(basedir.length()+1);
	} // end of if

	try {
	    template = vengine.getTemplate(path);
	} catch (Exception e) {
	    throw new MojoExecutionException("Fails to load template: " +
					     templateFile, e);

	} // end of catch

	FileWriter w = null;

	try {
	    w = new FileWriter(outputFile);

	    template.merge(ctx, w);
	} catch (Exception e) {
	    throw new MojoExecutionException("Fails to merge template", e);
	} finally {
	    if (w != null) {
		try {
		    w.close();
		} catch (Exception e) {
		    e.printStackTrace();
		} // end of catch
	    } // end of if
	} // end of finally
    } // end of processRule

    /**
     * Returns a velocity engine with resources system
     * based on files from given |directory|.
     *
     * @param directory Path to directory containing velocity 
     * engine resources
     * @return Inited velocity engine
     */
    private static VelocityEngine getVelocityEngine(String directory) 
        throws MojoExecutionException {

	Properties vprops = new Properties();
	
        vprops.setProperty("resource.loader", "file");
	
        vprops.setProperty("file.resource.loader.description",
                           "File resource loader");
	
        vprops.setProperty("file.resource.loader.class",
                           "org.apache.velocity.runtime.resource.loader." +
                           "FileResourceLoader");

        vprops.setProperty("file.resource.loader.path",
                           directory);


        vprops.setProperty("file.resource.loader.cache", "false");
        vprops.setProperty("file.resource.loader." +
			   "modificationCheckInterval", "2");
	
	try {
  	  VelocityEngine vengine = new VelocityEngine(vprops);
	
	  vengine.init();
	
	  return vengine;
	} catch (Exception e) {
	  throw new MojoExecutionException("Fails to init Velocity engine", e);
	} // end of catch
    } // end of getVelocityEngine
} // end of class GenerateMojo
