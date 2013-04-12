# Maven Template Plugin

This Maven plugin offers goal to generate final files from templates, using substitution, in a more custom/flexible way than `maven-resources-plugin`.

## Usage

To integrate in your POM:

```xml
<project>
  ...
  <repositories>
    ...
    <repository><!-- Plugin artifacts hosted at Applicius repo -->
      <id>applicius-releases</id>
      <name>Applicius Maven2 Releases Repository</name>
      <url>https://raw.github.com/applicius/mvn-repo/master/releases/</url>
    </repository>
  </repositories>
  ...

  <build>
    ...
    <plugins>
      <plugin>
        <groupId>cchantep</groupId>
	<artifactId>maven-template-plugin</artifactId>
	<version>1.0</version>

	<executions>
	  <execution>
	    <phase>generate-sources</phase>
	    <goals>
	      <goal>generate</goal>
	    </goals>
	  </execution>
	</executions>

	<configuration>
	  <rules>
	    <rule>
	      <template>src/main/templates/my_template.tmpl</template>
	      <output>output.file</output>
	      <filters>
	        <filter>src/main/filters/subst.properties</filter>
	      </filters>
	    </rule>

	    ...
	  </rules>
	</configuration>
      </plugin>
    </plugins>
  </build>
</project>
```

## Documentation

More documentation can be found [here](http://cchantep.github.com/maven-template-plugin/).
