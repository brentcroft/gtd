
/* model replaced here */
var steps = CUCUMBER_STEPS_ARRAY
var Pattern = java.util.regex.Pattern;
var CucumberStepDefinition = com.brentcroft.test.cucumber.CucumberStepDefinition;
for ( index in steps )
{
	var step = steps[ index ];
	
	backend.addStepDefinition( 
		new CucumberStepDefinition( 
			Pattern.compile( step.name ), 
			step
		) 
	)	
}