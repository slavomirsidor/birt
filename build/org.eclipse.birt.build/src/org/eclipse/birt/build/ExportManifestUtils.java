package org.eclipse.birt.build;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ExportManifestUtils
{
	static final String[] API_JAR_PATTERNS = new String[]{
		"chartengineapi.jar",
		"com.ibm.icu.*.jar",
		"org.apache.commons.codec_.*.jar", 
		"coreapi.jar", 
		"dataaggregationapi.jar",
		"dataadapterapi.jar",
		"dteapi.jar", 
		"engineapi.jar", 
		"flute.jar", 
		"js.jar",
		"modelapi.jar", 
		"crosstabcoreapi.jar",
		"dataextraction.jar",
		"org.eclipse.emf.common_.*.jar",
		"org.eclipse.emf.ecore.xmi_.*.jar", 
		"org.eclipse.emf.ecore_.*.jar",
		"org.w3c.css.sac_.*.jar", 
		"scriptapi.jar",
		"modelodaapi.jar",
		"odadesignapi.jar",
		"javax.servlet_.*.jar",
		"javax.servlet.jsp_.*.jar"
	};
	
	static final Boolean[] API_JAR_VERSIONS = new Boolean[]{
		false, //"chartengineapi.jar",
		true,//"com.ibm.icu.*.jar",
		false,//"org.apache.commons.codec_.*.jar", 
		false,//"coreapi.jar", 
		false,//"dataaggregationapi.jar",
		false,//"dataadapterapi.jar",
		false,//"dteapi.jar", 
		false,//"engineapi.jar", 
		false,//"flute.jar", 
		false,//"js.jar",
		false,//"modelapi.jar", 
		false,//"crosstabcoreapi.jar",
		false,//"dataextraction.jar",
		false,//"org.eclipse.emf.common_.*.jar",
		false,//"org.eclipse.emf.ecore.xmi_.*.jar", 
		false,//"org.eclipse.emf.ecore_.*.jar",
		false,//"org.w3c.css.sac_.*.jar", 
		false,//"scriptapi.jar",
		false,//"modelodaapi.jar",
		false,//"odadesignapi.jar",
		false,//"javax.servlet_.*.jar",
		false,//"javax.servlet.jsp_.*.jar"
	};

	static public void main( String[] args ) throws IOException
	{
		String jarFolder = ".";
		if ( args.length > 0 )
		{
			jarFolder = args[0];
		}
		File[] jarFiles = new File( jarFolder ).listFiles( );
		for ( int i = 0; i < jarFiles.length; i++ )
		{
			if ( isApiJar( jarFiles[i] ) )
			{
				exportPackages( jarFiles[i] );
			}
		}
	}

	static boolean isApiJar( File jarFile )
	{
		String name = jarFile.getName( );
		for ( int i = 0; i < API_JAR_PATTERNS.length; i++ )
		{
			if ( name.matches( API_JAR_PATTERNS[i] ) )
			{
				return true;
			}
		}
		return false;
	}
	
	static Boolean getVersion(File jarFile)
	{
		String name = jarFile.getName( );
		for ( int i = 0; i < API_JAR_PATTERNS.length; i++ )
		{
			if ( name.matches( API_JAR_PATTERNS[i] ) )
			{
				return API_JAR_VERSIONS[i];
			}
		}
		return null;
	}

	static void exportPackages( File jarFile ) throws IOException
	{
		System.out.println( "#" + jarFile.getName( ) );
		JarFile jar = new JarFile( jarFile );
		Entry root = new Entry( );
		String fileName = jarFile.getName();
		root.hasVersion = getVersion(jarFile);
		
		if (root.hasVersion)
		{
			/* get the version from jar name*/
			int startIdx;
			int endIdx;
			startIdx = fileName.indexOf("_");
			endIdx = fileName.lastIndexOf(".");
			root.version = fileName.substring(startIdx+1, endIdx-1);
			endIdx = root.version.lastIndexOf(".");
			root.version = root.version.substring(0, endIdx);
		}
		
		Enumeration entries = jar.entries( );
		while ( entries.hasMoreElements( ) )
		{
			JarEntry entry = (JarEntry) entries.nextElement( );
			createEntry( root, entry );
		}
		for ( int i = 0; i < root.children.size( ); i++ )
		{
			outputEntry( (Entry) root.children.get( i ), "" );
		}
	}

	static class Entry
	{

		String name;
		Boolean hasVersion;
		String version;
		boolean hasFiles;
		ArrayList children = new ArrayList( );
	}

	static void outputEntry( Entry entry, String prefix )
	{
		if ( entry.hasFiles )
		{
			if (entry.hasVersion){
				System.out.println( " " + prefix + "." + entry.name + ";version=\""+ entry.version +"\"," );
			}
			else
			{
				System.out.println( " " + prefix + "." + entry.name + "," );
			}
		}

		if ( prefix != null && prefix.length( ) != 0 )
		{
			prefix = prefix + "." + entry.name;
		}
		else
		{
			prefix = entry.name;
		}
		for ( int i = 0; i < entry.children.size( ); i++ )
		{
			outputEntry( (Entry) entry.children.get( i ), prefix );
		}
	}

	static void createEntry( Entry root, JarEntry entry )
	{
		if ( entry.isDirectory( ) )
		{
			String name = entry.getName( );
			String[] names = name.split( "/" );
			for ( int i = 0; i < names.length; i++ )
			{
				root = createEntry( root, names[i] );
			}
		}
		else
		{
			String name = entry.getName( );
			String[] names = name.split( "/" );
			for ( int i = 0; i < names.length - 1; i++ )
			{
				root = createEntry( root, names[i] );
			}
			root.hasFiles = true;
		}
	}

	static Entry createEntry( Entry parent, String name )
	{

		for ( int i = 0; i < parent.children.size( ); i++ )
		{
			Entry entry = (Entry) parent.children.get( i );
			if ( name.equals( entry.name ) )
			{
				return entry;
			}
		}
		Entry entry = new Entry( );
		entry.name = name;
		entry.version = parent.version;
		entry.hasVersion = parent.hasVersion;
		parent.children.add( entry );
		
		return entry;
	}
}
